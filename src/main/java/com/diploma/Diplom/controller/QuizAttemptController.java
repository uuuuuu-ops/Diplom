package com.diploma.Diplom.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.model.QuizAttempt;
import com.diploma.Diplom.service.QuizAttemptService;

@RestController
@RequestMapping("/quiz-attempts")
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    public QuizAttemptController(QuizAttemptService quizAttemptService) {
        this.quizAttemptService = quizAttemptService;
    }

    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<QuizAttempt> submitQuiz(
            @RequestParam String userId,
            @RequestParam String quizId,
            @RequestParam int correctAnswers,
            @RequestParam int totalQuestions
    ) {
        return ResponseEntity.ok(
                quizAttemptService.submitQuiz(userId, quizId, correctAnswers, totalQuestions)
        );
    }
}