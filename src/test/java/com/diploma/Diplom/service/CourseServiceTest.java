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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @InjectMocks CourseService courseService;

    private User approvedTeacher;

    @BeforeEach
    void setUp() {
        approvedTeacher = new User();
        approvedTeacher.setId("teacher-1");
        approvedTeacher.setEmail("teacher@test.com");
        approvedTeacher.setRole(Role.TEACHER);
        approvedTeacher.setTeacherApproved(true);
    }


    @Test
    @DisplayName("createCourse: платный курс — сохраняет с ценой")
    void createCourse_paidCourse_success() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Java Basics");
        req.setFree(false);
        req.setPrice(new BigDecimal("29.99"));

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.save(any(Course.class))).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher-1", req, null);

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

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher-1", req, null);

        assertThat(result.getPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("createCourse: платный курс без цены — BadRequestException")
    void createCourse_paidWithoutPrice_throws() {
        CreateCourseRequest req = new CreateCourseRequest();
        req.setTitle("Paid Course");
        req.setFree(false);
        req.setPrice(null);

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));

        assertThatThrownBy(() -> courseService.createCourse("teacher-1", req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Price is required");
    }

    @Test
    @DisplayName("createCourse: не преподаватель — ForbiddenException")
    void createCourse_notTeacher_throws() {
        User student = new User();
        student.setId("student-1");
        student.setRole(Role.STUDENT);
        when(userRepository.findById("student-1")).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> courseService.createCourse("student-1", new CreateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("createCourse: преподаватель не подтверждён — ForbiddenException")
    void createCourse_teacherNotApproved_throws() {
        approvedTeacher.setTeacherApproved(false);
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));

        assertThatThrownBy(() -> courseService.createCourse("teacher-1", new CreateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("createCourse: пользователь не найден — ResourceNotFoundException")
    void createCourse_userNotFound_throws() {
        when(userRepository.findById("ghost-id")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse("ghost-id", new CreateCourseRequest(), null))
                .isInstanceOf(ResourceNotFoundException.class);
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

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(cloudinaryService.uploadFile(file, "thumbnails")).thenReturn(uploadResult);
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.createCourse("teacher-1", req, file);

        assertThat(result.getThumbnail()).isEqualTo("https://cdn.example.com/thumb.jpg");
        assertThat(result.getThumbnailPublicId()).isEqualTo("thumbnails/abc123");
    }


    @Test
    @DisplayName("updateCourse: обновляет поля курса")
    void updateCourse_success() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");
        existing.setTitle("Old Title");

        UpdateCourseRequest req = new UpdateCourseRequest();
        req.setTitle("New Title");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Course result = courseService.updateCourse("teacher-1", "course-1", req, null);

        assertThat(result.getTitle()).isEqualTo("New Title");
    }

    @Test
    @DisplayName("updateCourse: чужой курс — ForbiddenException")
    void updateCourse_notOwner_throws() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("other-teacher");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> courseService.updateCourse("teacher-1", "course-1", new UpdateCourseRequest(), null))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("updateCourse: курс не найден — ResourceNotFoundException")
    void updateCourse_courseNotFound_throws() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse("teacher-1", "missing", new UpdateCourseRequest(), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("updateCourse: с новым превью — старый файл удаляется, новый загружается")
    void updateCourse_withNewThumbnail_replacesOld() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");
        existing.setThumbnailPublicId("thumbnails/old-id");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        CloudinaryService.FileUploadResult uploadResult = mock(CloudinaryService.FileUploadResult.class);
        when(uploadResult.getFileUrl()).thenReturn("https://cdn.example.com/new.jpg");
        when(uploadResult.getPublicId()).thenReturn("thumbnails/new-id");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(cloudinaryService.uploadFile(file, "thumbnails")).thenReturn(uploadResult);
        when(courseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        courseService.updateCourse("teacher-1", "course-1", new UpdateCourseRequest(), file);

        verify(cloudinaryService).deleteFile("thumbnails/old-id");
        verify(cloudinaryService).uploadFile(file, "thumbnails");
    }


    @Test
    @DisplayName("deleteCourse: удаляет курс и его уроки")
    void deleteCourse_success() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("teacher-1");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1")).thenReturn(Collections.emptyList());

        courseService.deleteCourse("teacher-1", "course-1");

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

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1")).thenReturn(Collections.emptyList());

        courseService.deleteCourse("teacher-1", "course-1");

        verify(cloudinaryService).deleteFile("thumbnails/abc123");
    }

    @Test
    @DisplayName("deleteCourse: чужой курс — ForbiddenException")
    void deleteCourse_notOwner_throws() {
        Course existing = new Course();
        existing.setId("course-1");
        existing.setTeacherId("other-teacher");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> courseService.deleteCourse("teacher-1", "course-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("deleteCourse: курс не найден — ResourceNotFoundException")
    void deleteCourse_courseNotFound_throws() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse("teacher-1", "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    
    @Test
    @DisplayName("getPublicCourses: без фильтров — возвращает страницу опубликованных курсов")
    void getPublicCourses_noFilters_returnsPage() {
        Course c1 = new Course(); c1.setPublished(true); c1.setTitle("Course A");
        Course c2 = new Course(); c2.setPublished(true); c2.setTitle("Course B");
        Pageable pageable = PageRequest.of(0, 20);
        Page<Course> page = new PageImpl<>(List.of(c1, c2), pageable, 2);

        when(courseRepository.findByPublishedTrue(pageable)).thenReturn(page);

        Page<Course> result = courseService.getPublicCourses(null, null, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("getPublicCourses: пустой результат — возвращает пустую страницу")
    void getPublicCourses_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(courseRepository.findByPublishedTrue(pageable)).thenReturn(Page.empty(pageable));

        Page<Course> result = courseService.getPublicCourses(null, null, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getPublicCourses: с фильтром по категории — вызывает findByPublishedTrueAndCategory")
    void getPublicCourses_withCategory_usesCorrectMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        when(courseRepository.findByPublishedTrueAndCategory("IT", pageable)).thenReturn(Page.empty(pageable));

        courseService.getPublicCourses("IT", null, pageable);

        verify(courseRepository).findByPublishedTrueAndCategory("IT", pageable);
        verify(courseRepository, never()).findByPublishedTrue(any(Pageable.class));
    }

    @Test
    @DisplayName("getPublicCourses: с категорией и уровнем — вызывает findByPublishedTrueAndCategoryAndLevel")
    void getPublicCourses_withCategoryAndLevel_usesCorrectMethod() {
        Pageable pageable = PageRequest.of(0, 10);
        when(courseRepository.findByPublishedTrueAndCategoryAndLevel("IT", "BEGINNER", pageable))
                .thenReturn(Page.empty(pageable));

        courseService.getPublicCourses("IT", "BEGINNER", pageable);

        verify(courseRepository).findByPublishedTrueAndCategoryAndLevel("IT", "BEGINNER", pageable);
    }


    @Test
    @DisplayName("getTeacherCourses: возвращает курсы конкретного преподавателя")
    void getTeacherCourses_returnsOwnCourses() {
        Course c = new Course();
        c.setTeacherId("teacher-1");

        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findByTeacherId("teacher-1")).thenReturn(List.of(c));

        List<Course> result = courseService.getTeacherCourses("teacher-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTeacherId()).isEqualTo("teacher-1");
    }

    @Test
    @DisplayName("getTeacherCourses: у преподавателя нет курсов — пустой список")
    void getTeacherCourses_noCourses() {
        when(userRepository.findById("teacher-1")).thenReturn(Optional.of(approvedTeacher));
        when(courseRepository.findByTeacherId("teacher-1")).thenReturn(Collections.emptyList());

        assertThat(courseService.getTeacherCourses("teacher-1")).isEmpty();
    }


    @Test
    @DisplayName("getCourseById: возвращает курс по ID")
    void getCourseById_success() {
        Course course = new Course();
        course.setId("course-1");
        course.setTitle("Test Course");

        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        Course result = courseService.getCourseById("course-1");

        assertThat(result.getId()).isEqualTo("course-1");
        assertThat(result.getTitle()).isEqualTo("Test Course");
    }

    @Test
    @DisplayName("getCourseById: курс не найден — ResourceNotFoundException")
    void getCourseById_notFound_throws() {
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourseById("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }
}