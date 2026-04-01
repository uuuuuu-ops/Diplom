package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateCourseRequest;
import com.diploma.Diplom.dto.UpdateCourseRequest;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public CourseService(CourseRepository courseRepository,
                         LessonRepository lessonRepository,
                         UserRepository userRepository,
                         FileStorageService fileStorageService) {
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

   public Course createCourse(String teacherEmail,
                           CreateCourseRequest request,
                           MultipartFile thumbnailFile) {

    User user = getApprovedTeacher(teacherEmail);

    Course course = new Course();
    course.setTeacherId(user.getId());
    course.setTitle(request.getTitle());
    course.setDescription(request.getDescription());
    course.setCategory(request.getCategory());
    course.setLevel(request.getLevel());
    course.setPublished(request.getPublished() != null ? request.getPublished() : false);

    course.setFree(request.isFree());

    if (request.isFree()) {
        course.setPrice(java.math.BigDecimal.ZERO);
    } else {
        if (request.getPrice() == null) {
            throw new RuntimeException("Price is required for paid course");
        }
        course.setPrice(request.getPrice());
    }

    course.setCurrency("USD");

    course.setCreatedAt(LocalDateTime.now());
    course.setUpdatedAt(LocalDateTime.now());

    if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
        FileStorageService.FileUploadResult uploaded =
                fileStorageService.saveFile(thumbnailFile, "thumbnails");
        course.setThumbnail(uploaded.getFileUrl());
    }

    return courseRepository.save(course);
}

    public List<Course> getTeacherCourses(String teacherEmail) {
        User user = getApprovedTeacher(teacherEmail);
        return courseRepository.findByTeacherId(user.getId());
    }

    public Course getCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));
    }

    public Course updateCourse(String teacherEmail,
                               String courseId,
                               UpdateCourseRequest request,
                               MultipartFile thumbnailFile) {
        User user = getApprovedTeacher(teacherEmail);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseOwnership(user, course);

        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getPublished() != null) course.setPublished(request.getPublished());

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            fileStorageService.deleteFile(course.getThumbnail());

            FileStorageService.FileUploadResult uploaded =
                    fileStorageService.saveFile(thumbnailFile, "thumbnails");
            course.setThumbnail(uploaded.getFileUrl());
        }

        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    public void deleteCourse(String teacherEmail, String courseId) {
        User user = getApprovedTeacher(teacherEmail);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseOwnership(user, course);

        fileStorageService.deleteFile(course.getThumbnail());
        lessonRepository.deleteAll(lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId));
        courseRepository.delete(course);
    }

    private User getApprovedTeacher(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new RuntimeException("Only teachers can manage courses");
        }
        if (!user.isTeacherApproved()) {
            throw new RuntimeException("Only approved teachers can manage courses");
        }

        return user;
    }

    private void validateCourseOwnership(User user, Course course) {
        if (!course.getTeacherId().equals(user.getId())) {
            throw new RuntimeException("You can manage only your own courses");
        }
    }

    public List<Course> getPublicCourses() {
    return courseRepository.findByPublishedTrue();
}

}