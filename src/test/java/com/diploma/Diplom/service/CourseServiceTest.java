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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseService Tests")
class CourseServiceTest {

    @Mock CourseRepository courseRepository;
    @Mock LessonRepository lessonRepository;
    @Mock UserRepository userRepository;
    @Mock CloudinaryService cloudinaryService;

    @InjectMocks
    CourseService courseService;

    private User approvedTeacher;

    @BeforeEach
    void setUp() {
        approvedTeacher = new User();
        approvedTeacher.setId("teacher-1");
        approvedTeacher.setEmail("teacher@test.com");
        approvedTeacher.setRole(Role.TEACHER);
        approvedTeacher.setTeacherApproved(true);
    }

    // ─────────────────────── createCourse ───────────────────────────────

    @Test
    @DisplayName("createCourse: платный курс — сохраняет с ценой")
    void createCourse_paidCourse_success() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Java Basics");
        req.setFree(false);
        req.setPrice(new BigDecimal("29.99"));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher@test.com", req, null);

        assertThat(result.getTitle()).isEqualTo("Java Basics");
        assertThat(result.getPrice()).isEqualByComparingTo("29.99");
        assertThat(result.getTeacherId()).isEqualTo("teacher-1");
    }

    @Test
    @DisplayName("createCourse: бесплатный курс — цена устанавливается в 0")
    void createCourse_freeCourse_priceSetToZero() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Free Course");
        req.setFree(true);

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher@test.com", req, null);

        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createCourse: платный курс без цены — выбрасывает BadRequestException")
    void createCourse_paidWithoutPrice_throws() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Paid Course");
        req.setFree(false);
        req.setPrice(null); // цена не указана

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));

        assertThatThrownBy(() -> courseService.createCourse("teacher@test.com", req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Price is required");
    }

    @Test
    @DisplayName("createCourse: пользователь не является преподавателем — ForbiddenException")
    void createCourse_notTeacher_throws() {
        User student = new User();
        student.setRole(Role.STUDENT);

        when(userRepository.findByEmail("student@test.com")).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> courseService.createCourse("student@test.com", new CreateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("createCourse: преподаватель не подтверждён — ForbiddenException")
    void createCourse_teacherNotApproved_throws() {
        approvedTeacher.setTeacherApproved(false);
        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));

        assertThatThrownBy(() -> courseService.createCourse("teacher@test.com", new CreateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("createCourse: с файлом превью — загружает в Cloudinary")
    void createCourse_withThumbnail_uploadsToCloudinary() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Course with Image");
        req.setFree(true);

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        CloudinaryService.FileUploadResult uploadResult = mock(CloudinaryService.FileUploadResult.class);
        when(uploadResult.getFileUrl()).thenReturn("https://cdn.example.com/thumb.jpg");
        when(uploadResult.getPublicId()).thenReturn("thumbnails/abc123");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(cloudinaryService.uploadFile(file, "thumbnails")).thenReturn(uploadResult);
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher@test.com", req, file);

        assertThat(result.getThumbnail()).isEqualTo("https://cdn.example.com/thumb.jpg");
        assertThat(result.getThumbnailPublicId()).isEqualTo("thumbnails/abc123");
    }

    // ─────────────────────── updateCourse ───────────────────────────────

    @Test
    @DisplayName("updateCourse: обновляет поля курса")
    void updateCourse_success() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");
        existing.setTitle("Old Title");

        UpdateCourseRequest req = new UpdateCourseRequest();
        req.setTitle("New Title");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.updateCourse("teacher@test.com", "course-1", req, null);

        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("updateCourse: чужой курс — ForbiddenException")
    void updateCourse_notOwner_throws() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("other-teacher");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> courseService.updateCourse("teacher@test.com", "course-1", new UpdateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("updateCourse: курс не найден — ResourceNotFoundException")
    void updateCourse_courseNotFound_throws() {
        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse("teacher@test.com", "missing", new UpdateCourseRequest(), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────── deleteCourse ───────────────────────────────

    @Test
    @DisplayName("deleteCourse: удаляет курс и его уроки")
    void deleteCourse_success() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1")).thenReturn(Collections.emptyList());

        courseService.deleteCourse("teacher@test.com", "course-1");

        verify(courseRepository).delete(existing);
        verify(lessonRepository).deleteAll(any());
    }

    @Test
    @DisplayName("deleteCourse: с превью — удаляет файл из Cloudinary")
    void deleteCourse_withThumbnail_deletesFromCloudinary() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");
        existing.setThumbnailPublicId("thumbnails/abc123");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1")).thenReturn(Collections.emptyList());

        courseService.deleteCourse("teacher@test.com", "course-1");

        verify(cloudinaryService).deleteFile("thumbnails/abc123");
    }

    // ─────────────────────── getPublicCourses ───────────────────────────

    @Test
    @DisplayName("getPublicCourses: возвращает только опубликованные курсы")
    void getPublicCourses_returnsPublished() {
        Course c1 = new Course(); c1.setPublished(true);
        Course c2 = new Course(); c2.setPublished(true);

        when(courseRepository.findByPublishedTrue()).thenReturn(List.of(c1, c2));

        List<Course> result = courseService.getPublicCourses();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(Course::isPublished);
    }

    // ─────────────────────── getTeacherCourses ───────────────────────────

    @Test
    @DisplayName("getTeacherCourses: возвращает курсы конкретного преподавателя")
    void getTeacherCourses_returnsOwnCourses() {
        Course c = new Course();
        c.setTeacherId("teacher-1");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findByTeacherId("teacher-1")).thenReturn(List.of(c));

        List<Course> result = courseService.getTeacherCourses("teacher@test.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeacherId()).isEqualTo("teacher-1");
    }
}