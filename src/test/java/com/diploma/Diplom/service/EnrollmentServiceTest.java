package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.*;
import com.diploma.Diplom.util.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Tests")
class EnrollmentServiceTest {

    @Mock EnrollmentRepository enrollmentRepository;
    @Mock CourseRepository courseRepository;
    @Mock SubscriptionService subscriptionService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks
    EnrollmentService enrollmentService;

    @Test
    @DisplayName("hasAccess: бесплатный курс — доступ всегда разрешён")
    void hasAccess_freeCourse_returnsTrue() {
        Course course = new Course();
        course.setFree(true);

        boolean result = enrollmentService.hasAccess("user-1", course);

        assertThat(result).isTrue();
        verifyNoInteractions(enrollmentRepository, subscriptionService);
    }

    @Test
    @DisplayName("hasAccess: есть активная запись — доступ разрешён")
    void hasAccess_activeEnrollment_returnsTrue() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(false);

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
                "user-1", "course-1", EnrollmentStatus.ACTIVE)).thenReturn(true);

        assertThat(enrollmentService.hasAccess("user-1", course)).isTrue();
    }

    @Test
    @DisplayName("hasAccess: нет записи, но есть активная подписка — доступ разрешён")
    void hasAccess_activeSubscription_returnsTrue() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(false);

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(any(), any(), any())).thenReturn(false);
        when(subscriptionService.hasActiveSubscription("user-1")).thenReturn(true);

        assertThat(enrollmentService.hasAccess("user-1", course)).isTrue();
    }

    @Test
    @DisplayName("hasAccess: нет записи и нет подписки — доступ запрещён")
    void hasAccess_noEnrollmentNoSubscription_returnsFalse() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(false);

        when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(any(), any(), any())).thenReturn(false);
        when(subscriptionService.hasActiveSubscription("user-1")).thenReturn(false);

        assertThat(enrollmentService.hasAccess("user-1", course)).isFalse();
    }

    // ─────────────────────── enrollFreeCourse ────────────────────────────

    @Test
    @DisplayName("enrollFreeCourse: регистрирует на бесплатный курс")
    void enrollFreeCourse_success() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(true);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Enrollment result = enrollmentService.enrollFreeCourse("course-1");

        assertThat(result.getAccessType()).isEqualTo(AccessType.FREE);
        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.ACTIVE);
    }

    @Test
    @DisplayName("enrollFreeCourse: уже записан — возвращает существующую запись")
    void enrollFreeCourse_alreadyEnrolled_returnsExisting() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(true);

        Enrollment existing = new Enrollment();
        existing.setUserId("user-1");
        existing.setCourseId("course-1");
        existing.setStatus(EnrollmentStatus.ACTIVE);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(enrollmentRepository.findByUserIdAndCourseIdAndStatus("user-1", "course-1", EnrollmentStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        Enrollment result = enrollmentService.enrollFreeCourse("course-1");

        assertThat(result).isSameAs(existing);
        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enrollFreeCourse: платный курс — ForbiddenException")
    void enrollFreeCourse_paidCourse_throws() {
        Course course = new Course();
        course.setId("course-1");
        course.setFree(false);

        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> enrollmentService.enrollFreeCourse("course-1"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("enrollFreeCourse: курс не найден — ResourceNotFoundException")
    void enrollFreeCourse_courseNotFound_throws() {
        when(securityUtils.getCurrentUserId()).thenReturn("user-1");
        when(courseRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.enrollFreeCourse("missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}


