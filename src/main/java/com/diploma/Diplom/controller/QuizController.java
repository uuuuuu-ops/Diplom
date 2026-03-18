package com.diploma.Diplom.controller;

import com.diploma.Diplom.model.Quiz;
import com.diploma.Diplom.service.QuizService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
    }

    @GetMapping("/lesson/{lessonId}")
    public List<Quiz> getQuizzesByLesson(@PathVariable String lessonId) {
        return quizService.getQuizzesByLesson(lessonId);
    }
}