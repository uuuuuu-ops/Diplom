package com.diploma.Diplom.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.dto.SubmitQuizRequest;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.ForbiddenException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Lesson;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.model.QuizAttempt;
import com.diploma.Diplom.model.QuizQuestion;
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

    public QuizAttempt submitQuiz(String userId, SubmitQuizRequest request,
                                  LocalDateTime startedAt) {

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));

        if (!quiz.isPublished()) {
            throw new ForbiddenException("This quiz is not published yet");
        }

        if (quiz.getTimeLimitSeconds() != null && startedAt != null) {
            long elapsedSeconds = java.time.Duration.between(startedAt, LocalDateTime.now())
                    .getSeconds();
            if (elapsedSeconds > quiz.getTimeLimitSeconds() + 10) {
                throw new BadRequestException(
                        "Time limit exceeded. Your answers were not recorded.");
            }
        }

        Lesson lesson = lessonRepository.findById(quiz.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        List<QuizQuestion> questions = quiz.getQuestions();
        List<Integer> answers = request.getAnswers();

        if (answers == null || answers.size() != questions.size()) {
            throw new BadRequestException(
                    "Answer count does not match question count. Expected "
                    + questions.size() + " answers.");
        }

        int correct = 0;
        for (int i = 0; i < questions.size(); i++) {
            if (questions.get(i).getCorrectAnswerIndex().equals(answers.get(i))) {
                correct++;
            }
        }

        int total = questions.size();
        int score = total == 0 ? 0 : (correct * 100) / total;
        int passingScore = quiz.getPassingScore() != null ? quiz.getPassingScore() : 60;
        boolean passed = score >= passingScore;

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(userId);
        attempt.setQuizId(quiz.getId());
        attempt.setLessonId(quiz.getLessonId());
        attempt.setCourseId(lesson.getCourseId());
        attempt.setCorrectAnswers(correct);
        attempt.setTotalQuestions(total);
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setSubmittedAt(LocalDateTime.now());

        attempt = quizAttemptRepository.save(attempt);

        if (passed) {
            courseProgressService.markQuizPassed(userId, lesson.getCourseId(), quiz.getId());
        }

        return attempt;
    }

    public List<QuizAttempt> getMyAttempts(String userId, String quizId) {
        return quizAttemptRepository.findByUserIdAndQuizId(userId, quizId);
    }
}