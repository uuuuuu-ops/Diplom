package com.diploma.Diplom.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "A recorded attempt by a student on a quiz") 
@Data
@Document(collection = "quiz_attempts")
public class QuizAttempt {
 
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "ID of the student who took the quiz")
    private String userId;

    @Schema(description = "ID of the quiz that was taken")
    private String quizId;

    @Schema(description = "ID of the lesson the quiz belongs to")
    private String lessonId;

    @Schema(description = "ID of the course the lesson belongs to")
    private String courseId;

    @Schema(description = "Score as a percentage 0–100", example = "80")
    private int score;
 
    @Schema(description = "Passed if score >= quiz's passingScore", example = "true")
    private boolean passed;

   @Schema(description = "Total number of questions in the quiz")
    private int totalQuestions;
    
    @Schema(description = "Number of correct answers given by the student")
    private int correctAnswers;
    
    @Schema(description = "Timestamp when the quiz was submitted", example = "2023-01-01T00:00:00")
    private LocalDateTime submittedAt;
}