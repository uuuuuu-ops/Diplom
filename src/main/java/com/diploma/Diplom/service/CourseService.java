package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateCourseRequest;
import com.diploma.Diplom.dto.UpdateCourseRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {

    private static final String DEFAULT_CURRENCY = "USD";
    private static final String THUMBNAILS_FOLDER = "thumbnails";

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    public Course createCourse(String userId,
                               CreateCourseRequest request,
                               MultipartFile thumbnailFile) {
        User user = getApprovedTeacher(userId);

        Course course = new Course();
        course.setTeacherId(user.getId());
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setCategory(request.getCategory());
        course.setLevel(request.getLevel());
        course.setPublished(request.getPublished() != null ? request.getPublished() : false);
        course.setFree(request.getFree());
        course.setCurrency(DEFAULT_CURRENCY);

        if (Boolean.TRUE.equals(request.getFree())) {
            course.setPrice(BigDecimal.ZERO);
        } else {
        if (request.getPrice() == null) {
            throw new BadRequestException("Price is required for paid course");
        }
        course.setPrice(request.getPrice());
        }

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            CloudinaryService.FileUploadResult uploaded =
                    cloudinaryService.uploadFile(thumbnailFile, THUMBNAILS_FOLDER);
            course.setThumbnail(uploaded.getFileUrl());
            course.setThumbnailPublicId(uploaded.getPublicId());
        }

        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());

        return courseRepository.save(course);
    }

    public List<Course> getTeacherCourses(String userId) {
        User user = getApprovedTeacher(userId);
        return courseRepository.findByTeacherId(user.getId());
    }

    public Course getCourseById(String courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));
    }

    public Course updateCourse(String userId,
                               String courseId,
                               UpdateCourseRequest request,
                               MultipartFile thumbnailFile) {
        User user = getApprovedTeacher(userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        validateCourseOwnership(user, course);

        if (request.getTitle() != null) course.setTitle(request.getTitle());
        if (request.getDescription() != null) course.setDescription(request.getDescription());
        if (request.getCategory() != null) course.setCategory(request.getCategory());
        if (request.getLevel() != null) course.setLevel(request.getLevel());
        if (request.getPublished() != null) course.setPublished(request.getPublished());

        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            if (course.getThumbnailPublicId() != null) {
                cloudinaryService.deleteFile(course.getThumbnailPublicId());
            }
            CloudinaryService.FileUploadResult uploaded =
                    cloudinaryService.uploadFile(thumbnailFile, THUMBNAILS_FOLDER);
            course.setThumbnail(uploaded.getFileUrl());
            course.setThumbnailPublicId(uploaded.getPublicId());
        }

        course.setUpdatedAt(LocalDateTime.now());
        return courseRepository.save(course);
    }

    public void deleteCourse(String userId, String courseId) {
        User user = getApprovedTeacher(userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + courseId));

        validateCourseOwnership(user, course);

        if (course.getThumbnailPublicId() != null) {
            cloudinaryService.deleteFile(course.getThumbnailPublicId());
        }

        lessonRepository.deleteAll(lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId));
        courseRepository.delete(course);
    }

    public List<Course> getPublicCourses() {
        return courseRepository.findByPublishedTrue();
    }

    private User getApprovedTeacher(String userId) {
    User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

    if (user.getRole() != Role.TEACHER) {
        throw new ForbiddenException("Only teachers can manage courses");
    }

    if (!user.isTeacherApproved()) {
        throw new ForbiddenException("Only approved teachers can manage courses");
    }

    return user;
}

    private void validateCourseOwnership(User user, Course course) {
        if (!course.getTeacherId().equals(user.getId())) {
            throw new ForbiddenException("You can manage only your own courses");
        }
    }
}
