package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateQuizRequest;
import com.diploma.Diplom.dto.UpdateQuizRequest;
import com.diploma.Diplom.model.Course;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.model.QuizQuestion;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.CourseRepository;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.QuizRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public QuizService(QuizRepository quizRepository,
                       LessonRepository lessonRepository,
                       CourseRepository courseRepository,
                       UserRepository userRepository) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.userRepository = userRepository;
    }

    public Quiz createQuiz(String teacherEmail, String lessonId, CreateQuizRequest request) {
        User user = getApprovedTeacher(teacherEmail);

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseOwnership(user, course);

        if (quizRepository.findByLessonId(lessonId).isPresent()) {
            throw new RuntimeException("A quiz already exists for this lesson. Use PUT to update it.");
        }

        validateQuestions(request.getQuestions());

        Quiz quiz = new Quiz();
        quiz.setLessonId(lessonId);
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setQuestions(request.getQuestions());
        quiz.setPassingScore(request.getPassingScore() != null ? request.getPassingScore() : 60);
        quiz.setTimeLimitSeconds(request.getTimeLimitSeconds()); // null = no limit
        quiz.setPublished(false);
        quiz.setCreatedAt(LocalDateTime.now());
        quiz.setUpdatedAt(LocalDateTime.now());

        return quizRepository.save(quiz);
    }

    public Quiz getQuizById(String quizId) {
        return quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));
    }

    public Quiz getQuizByLessonId(String lessonId) {
        return quizRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new RuntimeException("No quiz found for this lesson"));
    }

    public Quiz updateQuiz(String teacherEmail, String quizId, UpdateQuizRequest request) {
        User user = getApprovedTeacher(teacherEmail);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Lesson lesson = lessonRepository.findById(quiz.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseOwnership(user, course);

        if (request.getTitle() != null) quiz.setTitle(request.getTitle());
        if (request.getDescription() != null) quiz.setDescription(request.getDescription());
        if (request.getPassingScore() != null) quiz.setPassingScore(request.getPassingScore());

        if (request.getTimeLimitSeconds() != null) {
            // -1 signals "remove the time limit"
            quiz.setTimeLimitSeconds(request.getTimeLimitSeconds() == -1
                    ? null
                    : request.getTimeLimitSeconds());
        }

        if (request.getQuestions() != null) {
            validateQuestions(request.getQuestions());
            quiz.setQuestions(request.getQuestions());
        }

        if (request.getPublished() != null) quiz.setPublished(request.getPublished());

        quiz.setUpdatedAt(LocalDateTime.now());
        return quizRepository.save(quiz);
    }

    public void deleteQuiz(String teacherEmail, String quizId) {
        User user = getApprovedTeacher(teacherEmail);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Lesson lesson = lessonRepository.findById(quiz.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        Course course = courseRepository.findById(lesson.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        validateCourseOwnership(user, course);
        quizRepository.delete(quiz);
    }

    private void validateQuestions(List<QuizQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            throw new RuntimeException("Quiz must contain at least one question");
        }
        for (QuizQuestion q : questions) {
            if (q.getQuestion() == null || q.getQuestion().isBlank())
                throw new RuntimeException("Question text is required");
            if (q.getOptions() == null || q.getOptions().size() < 2)
                throw new RuntimeException("Each question must have at least two options");
            if (q.getCorrectAnswerIndex() == null)
                throw new RuntimeException("Correct answer index is required");
            if (q.getCorrectAnswerIndex() < 0 || q.getCorrectAnswerIndex() >= q.getOptions().size())
                throw new RuntimeException("Correct answer index is out of range");
        }
    }

    private User getApprovedTeacher(String teacherEmail) {
        User user = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getRole() != Role.TEACHER)
            throw new RuntimeException("Only teachers can manage quizzes");
        if (!user.isTeacherApproved())
            throw new RuntimeException("Only approved teachers can manage quizzes");
        return user;
    }

    private void validateCourseOwnership(User user, Course course) {
        if (!course.getTeacherId().equals(user.getId()))
            throw new RuntimeException("You can only manage quizzes in your own courses");
    }
}