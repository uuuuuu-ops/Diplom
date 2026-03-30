package com.diploma.Diplom.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.model.QuizAttempt;
import com.diploma.Diplom.repository.LessonRepository;
import com.diploma.Diplom.repository.QuizAttemptRepository;
import com.diploma.Diplom.repository.QuizRepository;

@Service
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final CourseProgressService courseProgressService;

    public QuizAttemptService(
            QuizRepository quizRepository,
            LessonRepository lessonRepository,
            QuizAttemptRepository quizAttemptRepository,
            CourseProgressService courseProgressService
    ) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.courseProgressService = courseProgressService;
    }

    public QuizAttempt submitQuiz(String userId, String quizId, int correctAnswers, int totalQuestions) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        Lesson lesson = lessonRepository.findById(quiz.getLessonId())
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        int score = totalQuestions == 0 ? 0 : (correctAnswers * 100) / totalQuestions;
        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 60;
        boolean passed = score >= passingScore;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quizId);
        attempt.setLessonId(quiz.getLessonId());
        attempt.setCourseId(lesson.getCourseId());
        attempt.setCorrectAnswers(correctAnswers);
        attempt.setTotalQuestions(totalQuestions);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(LocalDateTime.now());

        attempt = quizAttemptRepository.save(attempt);

        if (passed) {
            courseProgressService.markQuizPassed(userId, lesson.getCourseId(), quizId);
        }

        return attempt;
    }
}