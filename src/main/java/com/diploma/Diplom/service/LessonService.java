package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateLessonRequest;
import com.diploma.Diplom.dto.UpdateLessonRequest;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Lesson;
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
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService; 

    public LessonService(LessonRepository lessonRepository,
                         CourseRepository courseRepository,
                         UserRepository userRepository,
                         CloudinaryService cloudinaryService) { 
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Lesson addLessonToCourse(String userId,
                                    String courseId,
                                    CreateLessonRequest request,
                                    MultipartFile videoFile,
                                    MultipartFile lecturePdfFile) {
        User user = getApprovedTeacher(userId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        validateCourseOwnership(user, course);

        Lesson lesson = new Lesson();
        lesson.setCourseId(courseId);
        lesson.setTitle(request.getTitle());
        lesson.setDescription(request.getDescription());
        lesson.setOrderIndex(request.getOrderIndex());
        lesson.setDuration(request.getDuration());
        lesson.setLectureText(request.getLectureText());
        lesson.setPublished(false);
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setUpdatedAt(LocalDateTime.now());

        if (videoFile != null && !videoFile.isEmpty()) {
        if (lesson.getVideoPublicId() != null) {
            cloudinaryService.deleteFile(lesson.getVideoPublicId()); 
        }
        CloudinaryService.FileUploadResult uploaded =
            cloudinaryService.uploadFile(videoFile, "videos");
        lesson.setVideoUrl(uploaded.getFileUrl());
        lesson.setVideoFileName(uploaded.getFileName());
        lesson.setVideoPublicId(uploaded.getPublicId()); 
    }

        if (lecturePdfFile != null && !lecturePdfFile.isEmpty()) {
            if (lesson.getLecturePdfPublicId() != null) {
                cloudinaryService.deleteFile(lesson.getLecturePdfPublicId());
            }
        CloudinaryService.FileUploadResult uploaded =
                cloudinaryService.uploadFile(lecturePdfFile, "lecture-pdfs");
        lesson.setLecturePdfUrl(uploaded.getFileUrl());
        lesson.setLecturePdfFileName(uploaded.getFileName());
        lesson.setLecturePdfPublicId(uploaded.getPublicId()); 
    }

        return lessonRepository.save(lesson);
    }

    public List<Lesson> getLessonsByCourseId(String courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    public Lesson getLessonById(String lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }

    public Lesson updateLesson(String userId,
                               String lessonId,
                               UpdateLessonRequest request,
                               MultipartFile videoFile,
                               MultipartFile lecturePdfFile) {
        User user = getApprovedTeacher(userId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        validateCourseOwnership(user, course);

        if (request.getTitle() != null) lesson.setTitle(request.getTitle());
        if (request.getDescription() != null) lesson.setDescription(request.getDescription());
        if (request.getOrderIndex() != null) lesson.setOrderIndex(request.getOrderIndex());
        if (request.getDuration() != null) lesson.setDuration(request.getDuration());
        if (request.getLectureText() != null) lesson.setLectureText(request.getLectureText());
        if (request.getPublished() != null) lesson.setPublished(request.getPublished());

        if (videoFile != null && !videoFile.isEmpty()) {
            if (lesson.getVideoUrl() != null) {
                cloudinaryService.deleteFile(lesson.getVideoUrl());
            }
            CloudinaryService.FileUploadResult uploaded =
                    cloudinaryService.uploadFile(videoFile, "videos");
            lesson.setVideoUrl(uploaded.getFileUrl());
            lesson.setVideoFileName(uploaded.getFileName());
        }

        if (lecturePdfFile != null && !lecturePdfFile.isEmpty()) {
            if (lesson.getLecturePdfUrl() != null) {
                cloudinaryService.deleteFile(lesson.getLecturePdfUrl());
            }
            CloudinaryService.FileUploadResult uploaded =
                    cloudinaryService.uploadFile(lecturePdfFile, "lecture-pdfs");
            lesson.setLecturePdfUrl(uploaded.getFileUrl());
            lesson.setLecturePdfFileName(uploaded.getFileName());
        }

        lesson.setUpdatedAt(LocalDateTime.now());
        return lessonRepository.save(lesson);
    }

    public void deleteLesson(String userId, String lessonId) {
        User user = getApprovedTeacher(userId);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        validateCourseOwnership(user, course);

        if (lesson.getVideoPublicId() != null) 
            cloudinaryService.deleteFile(lesson.getVideoPublicId());
        if (lesson.getLecturePdfPublicId() != null) 
            cloudinaryService.deleteFile(lesson.getLecturePdfPublicId());
        lessonRepository.delete(lesson);
    }

    private User getApprovedTeacher(String userId) {
        User user = userRepository.findByEmail(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.TEACHER) {
            throw new ForbiddenException("Only teachers can manage lessons");
        }
        if (!user.isTeacherApproved()) {
            throw new ForbiddenException("Only approved teachers can manage lessons");
        }
        return user;
    }

    private void validateCourseOwnership(User user, Course course) {
        if (!course.getTeacherId().equals(user.getId())) {
            throw new ForbiddenException("You can manage only your own course lessons");
        }
    }
}