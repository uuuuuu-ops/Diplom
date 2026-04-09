package com.diploma.Diplom.service;

import static org.mockito.ArgumentMatchers.any;

import java.util.List;
import java.util.Optional;


import com.diploma.Diplom.dto.CreateQuizRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.*;
import com.diploma.Diplom.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuizService Tests")
class QuizServiceTest {

    @Mock QuizRepository quizRepository;
    @Mock LessonRepository lessonRepository;
    @Mock CourseRepository courseRepository;
    @Mock UserRepository userRepository;

    @InjectMocks
    QuizService quizService;

    private User approvedTeacher;
    private Lesson lesson;
    private Course course;

    @BeforeEach
    void setUp() {
        approvedTeacher = new User();
        approvedTeacher.setId("teacher-1");
        approvedTeacher.setEmail("teacher@test.com");
        approvedTeacher.setRole(Role.TEACHER);
        approvedTeacher.setTeacherApproved(true);

        lesson = new Lesson();
        lesson.setId("lesson-1");
        lesson.setCourseId("course-1");

        course = new Course();
        course.setId("course-1");
        course.setTeacherId("teacher-1");
    }

    private QuizQuestion validQuestion() {
        QuizQuestion q = new QuizQuestion();
        q.setQuestion("What is Java?");
        q.setOptions(List.of("A language", "A coffee", "A planet"));
        q.setCorrectAnswerIndex(0);
        return q;
    }

    // ─────────────────────── createQuiz ──────────────────────────────────

    @Test
    @DisplayName("createQuiz: успешное создание квиза")
    void createQuiz_success() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Java Quiz");
        req.setQuestions(List.of(validQuestion()));
        req.setPassingScore(70);

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());
        when(quizRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Quiz result = quizService.createQuiz("teacher@test.com", "lesson-1", req);

        assertThat(result.getTitle()).isEqualTo("Java Quiz");
        assertThat(result.getPassingScore()).isEqualTo(70);
        assertThat(result.isPublished()).isFalse();
    }


    @Test
    @DisplayName("createQuiz: квиз уже существует — BadRequestException")
    void createQuiz_alreadyExists_throws() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setQuestions(List.of(validQuestion()));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.of(new Quiz()));

        assertThatThrownBy(() -> quizService.createQuiz("teacher@test.com", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    @DisplayName("createQuiz: пустой список вопросов — BadRequestException")
    void createQuiz_noQuestions_throws() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Empty Quiz");
        req.setQuestions(List.of());

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.createQuiz("teacher@test.com", "lesson-1", req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("createQuiz: неверный индекс правильного ответа — BadRequestException")
    void createQuiz_invalidCorrectAnswerIndex_throws() {
        QuizQuestion q = new QuizQuestion();
        q.setQuestion("What is Java?");
        q.setOptions(List.of("A", "B"));
        q.setCorrectAnswerIndex(5); // выходит за пределы

        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Quiz");
        req.setQuestions(List.of(q));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.createQuiz("teacher@test.com", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("out of range");
    }

    @Test
    @DisplayName("createQuiz: вопрос с одним вариантом ответа — BadRequestException")
    void createQuiz_singleOption_throws() {
        QuizQuestion q = new QuizQuestion();
        q.setQuestion("What is Java?");
        q.setOptions(List.of("Only one option"));
        q.setCorrectAnswerIndex(0);

        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Quiz");
        req.setQuestions(List.of(q));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.createQuiz("teacher@test.com", "lesson-1", req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least two options");
    }

    @Test
    @DisplayName("createQuiz: чужой курс — ForbiddenException")
    void createQuiz_notCourseOwner_throws() {
        course.setTeacherId("other-teacher");

        CreateQuizRequest req = new CreateQuizRequest();
        req.setQuestions(List.of(validQuestion()));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        assertThatThrownBy(() -> quizService.createQuiz("teacher@test.com", "lesson-1", req))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("createQuiz: passingScore по умолчанию 60, если не указан")
    void createQuiz_defaultPassingScore() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setTitle("Quiz");
        req.setPassingScore(null);
        req.setQuestions(List.of(validQuestion()));

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));
        when(quizRepository.findByLessonId("lesson-1")).thenReturn(Optional.empty());
        when(quizRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Quiz result = quizService.createQuiz("teacher@test.com", "lesson-1", req);

        assertThat(result.getPassingScore()).isEqualTo(60);
    }

    // ─────────────────────── deleteQuiz ──────────────────────────────────

    @Test
    @DisplayName("deleteQuiz: успешное удаление")
    void deleteQuiz_success() {
        Quiz quiz = new Quiz();
        quiz.setId("quiz-1");
        quiz.setLessonId("lesson-1");

        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(quizRepository.findById("quiz-1")).thenReturn(Optional.of(quiz));
        when(lessonRepository.findById("lesson-1")).thenReturn(Optional.of(lesson));
        when(courseRepository.findById("course-1")).thenReturn(Optional.of(course));

        quizService.deleteQuiz("teacher@test.com", "quiz-1");

        verify(quizRepository).delete(quiz);
    }

    @Test
    @DisplayName("deleteQuiz: квиз не найден — ResourceNotFoundException")
    void deleteQuiz_notFound_throws() {
        when(userRepository.findByEmail("teacher@test.com")).thenReturn(Optional.of(approvedTeacher));
        when(quizRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quizService.deleteQuiz("teacher@test.com", "missing"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}