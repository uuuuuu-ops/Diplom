package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.CourseProgress;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.repository.CertificateRepository;
import com.diploma.Diplom.repository.CourseProgressRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CourseProgressService Tests")
class CourseProgressServiceTest {

    @Mock CourseProgressRepository courseProgressRepository;
    @Mock LessonRepository lessonRepository;
    @Mock QuizRepository quizRepository;
    @Mock CertificateRepository certificateRepository;
    @Mock CertificateService certificateService;

    @InjectMocks
    CourseProgressService courseProgressService;

    private Lesson lesson;
    private CourseProgress existingProgress;

    @BeforeEach
    void setUp() {
        lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setCourseId("course-1");
        lesson.setOrderIndex(0);
        lesson.setPublished(true);
        lesson.setQuizRequired(false);

        existingProgress = new CourseProgress();
        existingProgress.setId("prog-1");
        existingProgress.setUserId("user-1");
        existingProgress.setCourseId("course-1");
        existingProgress.setCompletedLessonIds(new HashSet<>());
        existingProgress.setPassedQuizIds(new HashSet<>());
        existingProgress.setProgressPercent(0);
        existingProgress.setCompleted(false);
    }

    // ─────────────────────── markLessonCompleted ─────────────────────────

    @Test
    @DisplayName("markLessonCompleted: простой урок без квиза — добавляет в completedLessonIds")
    void markLessonCompleted_noQuiz_success() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));
        when(quizRepository.findByLessonIdIn(any())).thenReturn(List.of());
        when(certificateRepository.findByUserIdAndCourseId(any(), any())).thenReturn(Optional.empty());
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.markLessonCompleted("user-1", "course-1", "lesson-1");

        assertThat(result.getCompletedLessonIds()).contains("lesson-1");
        verify(courseProgressRepository).save(any());
    }

    @Test
    @DisplayName("markLessonCompleted: урок не найден — ResourceNotFoundException")
    void markLessonCompleted_lessonNotFound_throws() {
        when(lessonRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseProgressService.markLessonCompleted("user-1", "course-1", "missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Lesson not found");
    }

    @Test
    @DisplayName("markLessonCompleted: квиз обязателен, не пройден — ForbiddenException")
    void markLessonCompleted_quizRequired_notPassed_throws() {
        lesson.setQuizRequired(true);
        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setLessonId("lesson-1");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(quiz));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress)); // passedQuizIds пуст

        assertThatThrownBy(() -> courseProgressService.markLessonCompleted("user-1", "course-1", "lesson-1"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("pass this lesson's quiz");
    }

    @Test
    @DisplayName("markLessonCompleted: квиз обязателен и пройден — успешно")
    void markLessonCompleted_quizRequired_passed_success() {
        lesson.setQuizRequired(true);
        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setLessonId("lesson-1");

        existingProgress.getPassedQuizIds().add("quiz-1");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(quiz));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));
        when(quizRepository.findByLessonIdIn(any())).thenReturn(List.of(quiz));
        when(certificateRepository.findByUserIdAndCourseId(any(), any())).thenReturn(Optional.empty());
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.markLessonCompleted("user-1", "course-1", "lesson-1");

        assertThat(result.getCompletedLessonIds()).contains("lesson-1");
    }

    @Test
    @DisplayName("markLessonCompleted: все уроки завершены — выдаёт сертификат")
    void markLessonCompleted_allDone_issuesCertificate() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));
        when(quizRepository.findByLessonIdIn(any())).thenReturn(List.of());
        when(certificateRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        courseProgressService.markLessonCompleted("user-1", "course-1", "lesson-1");

        // Курс завершён — сертификат должен быть выдан
        verify(certificateService).issueCertificate("user-1", "course-1");
    }

    @Test
    @DisplayName("markLessonCompleted: сертификат уже существует — повторно не выдаётся")
    void markLessonCompleted_certificateAlreadyExists_noReissue() {
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));
        when(quizRepository.findByLessonIdIn(any())).thenReturn(List.of());
        when(certificateRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(new com.diploma.Diplom.model.Certificate())); // уже есть
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        courseProgressService.markLessonCompleted("user-1", "course-1", "lesson-1");

        verify(certificateService, never()).issueCertificate(any(), any());
    }

    // ─────────────────────── markQuizPassed ──────────────────────────────

    @Test
    @DisplayName("markQuizPassed: добавляет квиз в passedQuizIds")
    void markQuizPassed_success() {
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
              .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(lesson));
        when(quizRepository.findByLessonIdIn(any())).thenReturn(List.of());
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.markQuizPassed("user-1", "course-1", "quiz-1");

        assertThat(result.getPassedQuizIds()).contains("quiz-1");
}

    // ─────────────────────── isLessonUnlocked ────────────────────────────

    @Test
    @DisplayName("isLessonUnlocked: первый урок (orderIndex=0) — всегда разблокирован")
    void isLessonUnlocked_firstLesson_alwaysTrue() {
        lesson.setOrderIndex(0);

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        boolean result = courseProgressService.isLessonUnlocked("user-1", "course-1", "lesson-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isLessonUnlocked: предыдущий урок не завершён — заблокирован")
    void isLessonUnlocked_previousNotCompleted_returnsFalse() {
        Lesson prev = new Lesson();
        prev.setId("lesson-0");
        prev.setOrderIndex(0);
        prev.setPublished(true);
        prev.setQuizRequired(false);

        Lesson target = new Lesson();
        target.setId("lesson-1");
        target.setOrderIndex(1);
        target.setPublished(true);

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(prev, target));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress)); // ничего не завершено

        boolean result = courseProgressService.isLessonUnlocked("user-1", "course-1", "lesson-1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isLessonUnlocked: предыдущий урок завершён — разблокирован")
    void isLessonUnlocked_previousCompleted_returnsTrue() {
        Lesson prev = new Lesson();
        prev.setId("lesson-0");
        prev.setOrderIndex(0);
        prev.setPublished(true);
        prev.setQuizRequired(false);

        Lesson target = new Lesson();
        target.setId("lesson-1");
        target.setOrderIndex(1);
        target.setPublished(true);

        existingProgress.getCompletedLessonIds().add("lesson-0");

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(prev, target));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));

        boolean result = courseProgressService.isLessonUnlocked("user-1", "course-1", "lesson-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isLessonUnlocked: предыдущий урок с квизом, квиз не пройден — заблокирован")
    void isLessonUnlocked_previousQuizNotPassed_returnsFalse() {
        Lesson prev = new Lesson();
        prev.setId("lesson-0");
        prev.setOrderIndex(0);
        prev.setPublished(true);
        prev.setQuizRequired(true);

        Lesson target = new Lesson();
        target.setId("lesson-1");
        target.setOrderIndex(1);
        target.setPublished(true);

        Quiz quiz = new Quiz();
        quiz.setId("quiz-0");
        quiz.setLessonId("lesson-0");

        existingProgress.getCompletedLessonIds().add("lesson-0");
        // quiz не в passedQuizIds

        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(target));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(prev, target));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(quizRepository.findByLessonId("lesson-0")).thenReturn(Optional.of(quiz));

        boolean result = courseProgressService.isLessonUnlocked("user-1", "course-1", "lesson-1");

        assertThat(result).isFalse();
    }

    // ─────────────────────── progressPercent calculation ─────────────────

    @Test
    @DisplayName("progressPercent: 1 из 2 уроков — 50%")
    void progressPercent_halfDone() {
        Lesson l1 = new Lesson(); l1.setId("l1"); l1.setOrderIndex(0); l1.setPublished(true);
        Lesson l2 = new Lesson(); l2.setId("l2"); l2.setOrderIndex(1); l2.setPublished(true);

        existingProgress.getCompletedLessonIds().add("l1");

        when(lessonRepository.findById("l1")).thenReturn(Optional.of(l1));
        when(courseProgressRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existingProgress));
        when(lessonRepository.findByCourseIdOrderByOrderIndexAsc("course-1"))
                .thenReturn(List.of(l1, l2));
        when(courseProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CourseProgress result = courseProgressService.markLessonCompleted("user-1", "course-1", "l1");

        assertThat(result.getProgressPercent()).isEqualTo(50);
    }
}