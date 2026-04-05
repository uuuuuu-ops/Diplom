package com.diploma.Diplom.controller;

import com.diploma.Diplom.model.TeacherQuizAttempt;
import com.diploma.Diplom.model.TeacherQuizQuestion;
import com.diploma.Diplom.service.TeacherQuizService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher/quiz")
public class TeacherQuizController {

    private final TeacherQuizService quizService;

    public TeacherQuizController(TeacherQuizService quizService) {
        this.quizService = quizService;
    }

    // Получить вопросы квиза
    @GetMapping("/{applicationId}/questions")
    public ResponseEntity<List<TeacherQuizQuestion>> getQuestions(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(quizService.getQuestions(applicationId));
    }

    // Отправить ответы
    @PostMapping("/{applicationId}/submit")
    public ResponseEntity<TeacherQuizAttempt> submitQuiz(
            @PathVariable String applicationId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Integer> answers) {

        return ResponseEntity.ok(
            quizService.submitQuiz(userDetails.getUsername(), applicationId, answers)
        );
    }

    // Получить результат
    @GetMapping("/{applicationId}/result")
    public ResponseEntity<TeacherQuizAttempt> getResult(
            @PathVariable String applicationId) {
        return ResponseEntity.ok(quizService.getMyAttempt(applicationId));
    }
}