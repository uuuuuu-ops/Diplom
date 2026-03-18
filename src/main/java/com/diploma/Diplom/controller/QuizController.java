package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CreateQuizRequest;
import com.diploma.Diplom.dto.UpdateQuizRequest;
import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.service.QuizService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Quiz createQuiz(Authentication authentication,
                           @PathVariable String lessonId,
                           @RequestBody CreateQuizRequest request) {
        return quizService.createQuiz(authentication.getName(), lessonId, request);
    }

    @GetMapping("/{quizId}")
    public Quiz getQuizById(@PathVariable String quizId) {
        return quizService.getQuizById(quizId);
    }

    @GetMapping("/lesson/{lessonId}")
    public Quiz getQuizByLessonId(@PathVariable String lessonId) {
        return quizService.getQuizByLessonId(lessonId);
    }

    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public Quiz updateQuiz(Authentication authentication,
                           @PathVariable String quizId,
                           @RequestBody UpdateQuizRequest request) {
        return quizService.updateQuiz(authentication.getName(), quizId, request);
    }

    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('TEACHER')")
    public String deleteQuiz(Authentication authentication,
                             @PathVariable String quizId) {
        quizService.deleteQuiz(authentication.getName(), quizId);
        return "Quiz deleted successfully";
    }
}