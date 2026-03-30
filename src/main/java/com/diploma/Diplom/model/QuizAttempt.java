package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "quiz_attempts")
public class QuizAttempt {

    @Id
    private String id;

    private String userId;
    private String quizId;
    private String lessonId;
    private String courseId;

    private int score;
    private int totalQuestions;
    private int correctAnswers;
    private boolean passed;

    private LocalDateTime submittedAt;
}