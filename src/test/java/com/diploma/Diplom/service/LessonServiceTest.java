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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LessonService Tests")
class LessonServiceTest {

    @Mock LessonRepository lessonRepository;
    @Mock CourseRepository courseRepository;
    @Mock UserRepository userRepository;
    @Mock CloudinaryService cloudinaryService;

    @InjectMocks LessonService lessonService;

    private User teacher;
    private Course course;
    private Lesson lesson;
    private CreateLessonRequest createRequest;

    @BeforeEach
    void setUp() {
        teacher = new User();
        teacher.setId("teacher-1");
        teacher.setRole(Role.TEACHER);
        teacher.setTeacherApproved(true);

        course = new Course();
        course.setId("course-1");
        course.setTeacherId("teacher-1");

        lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setCourseId("course-1");
        lesson.setTitle("Introduction");
        lesson.setOrderIndex(0);

        createRequest = new CreateLessonRequest();
        createRequest.setTitle("Introduction");
        createRequest.setDescription("Learn the basics");
        createRequest.setOrderIndex(0);
        createRequest.setDuration(15);
        createRequest.setLectureText("Welcome to the course!");
    }

    // ──────────────────── addLessonToCourse ──────────────────────────────

    @Test
    @DisplayName("addLessonToCourse: успех — урок сохраняется")
    void addLesson_success_savesLesson() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(lessonRepository.save(any(Lesson.class))).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, null);

        assertThat(result.getTitle()).isEqualTo("Introduction");
        assertThat(result.getCourseId()).isEqualTo("course-1");
        assertThat(result.isPublished()).isFalse();
        verify(lessonRepository).save(any(Lesson.class));
    }

    @Test
    @DisplayName("addLessonToCourse: пользователь не найден — ResourceNotFoundException")
    void addLesson_userNotFound_throws() {
        when(userRepository.findById("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.addLessonToCourse("nobody", "course-1", createRequest, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addLessonToCourse: роль не TEACHER — ForbiddenException")
    void addLesson_notTeacher_throws() {
        teacher.setRole(Role.STUDENT);
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() ->
                lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only teachers");
    }

    @Test
    @DisplayName("addLessonToCourse: учитель не одобрен — ForbiddenException")
    void addLesson_teacherNotApproved_throws() {
        teacher.setTeacherApproved(false);
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() ->
                lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("approved");
    }

    @Test
    @DisplayName("addLessonToCourse: курс не найден — ResourceNotFoundException")
    void addLesson_courseNotFound_throws() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("addLessonToCourse: учитель не владелец курса — ForbiddenException")
    void addLesson_notCourseOwner_throws() {
        course.setTeacherId("other-teacher");
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() ->
                lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("your own");
    }

    @Test
    @DisplayName("addLessonToCourse: с видеофайлом — загружает в Cloudinary")
    void addLesson_withVideo_uploadsToCloudinary() {
        MockMultipartFile video = new MockMultipartFile("video", "test.mp4", "video/mp4", "data".getBytes());

        CloudinaryService.FileUploadResult uploadResult = new CloudinaryService.FileUploadResult();
        uploadResult.setFileUrl("https://cloudinary.com/video.mp4");
        uploadResult.setPublicId("videos/test");
        uploadResult.setFileName("test.mp4");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(cloudinaryService.uploadFile(video, "videos")).thenReturn(uploadResult);
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, video, null);

        assertThat(result.getVideoUrl()).isEqualTo("https://cloudinary.com/video.mp4");
        assertThat(result.getVideoPublicId()).isEqualTo("videos/test");
        verify(cloudinaryService).uploadFile(video, "videos");
    }

    @Test
    @DisplayName("addLessonToCourse: с PDF-файлом — загружает в Cloudinary")
    void addLesson_withPdf_uploadsToCloudinary() {
        MockMultipartFile pdf = new MockMultipartFile("pdf", "lecture.pdf", "application/pdf", "data".getBytes());

        CloudinaryService.FileUploadResult uploadResult = new CloudinaryService.FileUploadResult();
        uploadResult.setFileUrl("https://cloudinary.com/lecture.pdf");
        uploadResult.setPublicId("lecture-pdfs/lecture");
        uploadResult.setFileName("lecture.pdf");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(cloudinaryService.uploadFile(pdf, "lecture-pdfs")).thenReturn(uploadResult);
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.addLessonToCourse("teacher-1", "course-1", createRequest, null, pdf);

        assertThat(result.getLecturePdfUrl()).isEqualTo("https://cloudinary.com/lecture.pdf");
        verify(cloudinaryService).uploadFile(pdf, "lecture-pdfs");
    }

    // ──────────────────── getLessonsByCourseId ────────────────────────────

    @Test
    @DisplayName("getLessonsByCourseId: возвращает список уроков по курсу")
    void getLessonsByCourseId_returnsList() {
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));

        List<Lesson> result = lessonService.getLessonsByCourseId("course-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("lesson-1");
    }

    // ──────────────────── getLessonById ───────────────────────────────────

    @Test
    @DisplayName("getLessonById: урок найден — возвращает урок")
    void getLessonById_found_returnsLesson() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        Lesson result = lessonService.getLessonById("lesson-1");

        assertThat(result.getId()).isEqualTo("lesson-1");
    }

    @Test
    @DisplayName("getLessonById: урок не найден — ResourceNotFoundException")
    void getLessonById_notFound_throws() {
        when(lessonRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.getLessonById("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────── updateLesson ────────────────────────────────────

    @Test
    @DisplayName("updateLesson: обновляет поля урока")
    void updateLesson_success_updatesFields() {
        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setTitle("New Title");
        req.setDescription("New desc");
        req.setOrderIndex(1);
        req.setDuration(30);
        req.setLectureText("Updated text");
        req.setPublished(true);
        req.setQuizRequired(false);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.updateLesson("teacher-1", "lesson-1", req, null, null);

        assertThat(result.getTitle()).isEqualTo("New Title");
        assertThat(result.isPublished()).isTrue();
        assertThat(result.getDuration()).isEqualTo(30);
        verify(lessonRepository).save(any());
    }

    @Test
    @DisplayName("updateLesson: урок не найден — ResourceNotFoundException")
    void updateLesson_lessonNotFound_throws() {
        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setPublished(true);
        req.setQuizRequired(false);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                lessonService.updateLesson("teacher-1", "missing", req, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateLesson: не владелец курса — ForbiddenException")
    void updateLesson_notOwner_throws() {
        course.setTeacherId("other");
        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setPublished(false);
        req.setQuizRequired(false);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() ->
                lessonService.updateLesson("teacher-1", "lesson-1", req, null, null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("updateLesson: с новым видео — удаляет старое и загружает новое")
    void updateLesson_withNewVideo_replacesOldFile() {
        lesson.setVideoPublicId("old-public-id");

        MockMultipartFile newVideo = new MockMultipartFile("video", "new.mp4", "video/mp4", "data".getBytes());
        CloudinaryService.FileUploadResult uploaded = new CloudinaryService.FileUploadResult();
        uploaded.setFileUrl("https://cloudinary.com/new.mp4");
        uploaded.setPublicId("videos/new");
        uploaded.setFileName("new.mp4");

        UpdateLessonRequest req = new UpdateLessonRequest();
        req.setPublished(false);
        req.setQuizRequired(false);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(cloudinaryService.uploadFile(newVideo, "videos")).thenReturn(uploaded);
        when(lessonRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Lesson result = lessonService.updateLesson("teacher-1", "lesson-1", req, newVideo, null);

        verify(cloudinaryService).deleteFile("old-public-id");
        verify(cloudinaryService).uploadFile(newVideo, "videos");
        assertThat(result.getVideoPublicId()).isEqualTo("videos/new");
    }

    // ──────────────────── deleteLesson ────────────────────────────────────

    @Test
    @DisplayName("deleteLesson: успех — удаляет файлы и запись")
    void deleteLesson_success_deletesAll() {
        lesson.setVideoPublicId("video-id");
        lesson.setLecturePdfPublicId("pdf-id");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        lessonService.deleteLesson("teacher-1", "lesson-1");

        verify(cloudinaryService).deleteFile("video-id");
        verify(cloudinaryService).deleteFile("pdf-id");
        verify(lessonRepository).delete(lesson);
    }

    @Test
    @DisplayName("deleteLesson: без файлов — удаляет только запись")
    void deleteLesson_noFiles_deletesOnlyRecord() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        lessonService.deleteLesson("teacher-1", "lesson-1");

        verify(cloudinaryService, never()).deleteFile(any());
        verify(lessonRepository).delete(lesson);
    }

    @Test
    @DisplayName("deleteLesson: урок не найден — ResourceNotFoundException")
    void deleteLesson_lessonNotFound_throws() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lessonService.deleteLesson("teacher-1", "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteLesson: не владелец курса — ForbiddenException")
    void deleteLesson_notOwner_throws() {
        course.setTeacherId("someone-else");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(teacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> lessonService.deleteLesson("teacher-1", "lesson-1"))
                .isInstanceOf(ForbiddenException.class);
    }
}