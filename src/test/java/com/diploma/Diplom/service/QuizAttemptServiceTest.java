package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.SubmitQuizRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.QuizAttemptRepository;
import com.diploma.Diplom.repository.QuizRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizAttemptService Tests")
class QuizAttemptServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock LessonRepository lessonRepository;
    @Mock QuizAttemptRepository quizAttemptRepository;
    @Mock CourseProgressService courseProgressService;

    @InjectMocks
    QuizAttemptService quizAttemptService;

    private Quiz publishedQuiz;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        QuizQuestion q1 = new QuizQuestion();
        q1.setQuestion("What is 2+2?");
        q1.setOptions(List.of("3", "4", "5"));
        q1.setCorrectAnswerIndex(1);

        QuizQuestion q2 = new QuizQuestion();
        q2.setQuestion("What is the sky?");
        q2.setOptions(List.of("Blue", "Green"));
        q2.setCorrectAnswerIndex(0);

        publishedQuiz = new Quiz();
        publishedQuiz.setId("quiz-1");
        publishedQuiz.setLessonId("lesson-1");
        publishedQuiz.setPublished(true);
        publishedQuiz.setPassingScore(50);
        publishedQuiz.setQuestions(List.of(q1, q2));

        lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setCourseId("course-1");
    }


    @Test
    @DisplayName("submitQuiz: все ответы верны — score=100, passed=true")
    void submitQuiz_allCorrect() {
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1, 0)); // оба правильных

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.submitQuiz("user-1", req, null);

        assertThat(result.getScore()).isEqualTo(100);
        assertThat(result.isPassed()).isTrue();
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        verify(courseProgressService).markQuizPassed("user-1", "course-1", "quiz-1");
    }

    @Test
    @DisplayName("submitQuiz: все ответы неверны — score=0, passed=false")
    void submitQuiz_allWrong() {
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(0, 1)); 

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.submitQuiz("user-1", req, null);

        assertThat(result.getScore()).isEqualTo(0);
        assertThat(result.isPassed()).isFalse();
        verify(courseProgressService, never()).markQuizPassed(any(), any(), any());
    }

    @Test
    @DisplayName("submitQuiz: квиз не опубликован — ForbiddenException")
    void submitQuiz_notPublished_throws() {
        publishedQuiz.setPublished(false);

        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1, 0));

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));

        assertThatThrownBy(() -> quizAttemptService.submitQuiz("user-1", req, null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("not published");
    }

    @Test
    @DisplayName("submitQuiz: квиз не найден — ResourceNotFoundException")
    void submitQuiz_quizNotFound_throws() {
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("missing");
        req.setAnswers(List.of());

        when(quizRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizAttemptService.submitQuiz("user-1", req, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("submitQuiz: неверное количество ответов — BadRequestException")
    void submitQuiz_wrongAnswerCount_throws() {
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1)); 

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> quizAttemptService.submitQuiz("user-1", req, null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Answer count");
    }

    @Test
    @DisplayName("submitQuiz: ответы null — BadRequestException")
    void submitQuiz_nullAnswers_throws() {
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(null);

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));

        assertThatThrownBy(() -> quizAttemptService.submitQuiz("user-1", req, null))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("submitQuiz: таймер превышен — BadRequestException")
    void submitQuiz_timeLimitExceeded_throws() {
        publishedQuiz.setTimeLimitSeconds(60);

        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1, 0));

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(5);

        assertThatThrownBy(() -> quizAttemptService.submitQuiz("user-1", req, startedAt))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Time limit exceeded");
    }

    @Test
    @DisplayName("submitQuiz: таймер в пределах нормы (с 10с grace period) — успешно")
    void submitQuiz_withinTimeLimit_success() {
        publishedQuiz.setTimeLimitSeconds(300);

        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1, 0));

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime startedAt = LocalDateTime.now().minusMinutes(1);

        assertThatNoException().isThrownBy(() ->
                quizAttemptService.submitQuiz("user-1", req, startedAt));
    }

    @Test
    @DisplayName("submitQuiz: passingScore по умолчанию 60, если в квизе не задан")
    void submitQuiz_defaultPassingScore() {
        publishedQuiz.setPassingScore(null);
        SubmitQuizRequest req = new SubmitQuizRequest();
        req.setQuizId("quiz-1");
        req.setAnswers(List.of(1, 1)); 

        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(publishedQuiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(quizAttemptRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        QuizAttempt result = quizAttemptService.submitQuiz("user-1", req, null);

        assertThat(result.getScore()).isEqualTo(50);
        assertThat(result.isPassed()).isFalse();
    }


    @Test
    @DisplayName("getMyAttempts: возвращает попытки пользователя по квизу")
    void getMyAttempts_returnsAttempts() {
        QuizAttempt a1 = new QuizAttempt();
        a1.setUserId("user-1");
        a1.setQuizId("quiz-1");
        a1.setScore(80);

        when(quizAttemptRepository.findByUserIdAndQuizId("user-1", "quiz-1"))
                .thenReturn(List.of(a1));

        List<QuizAttempt> result = quizAttemptService.getMyAttempts("user-1", "quiz-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getScore()).isEqualTo(80);
    }
}