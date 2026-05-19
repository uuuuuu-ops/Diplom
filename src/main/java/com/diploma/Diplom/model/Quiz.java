package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A quiz attached to a lesson")
@Data
@Document(collection = "quizzes")
public class Quiz {
    @Schema(description = "MongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "ID of the lesson this quiz belongs to")
    private String lessonId; 

    @Schema(description = "Quiz title displayed to students", example = "Java Basics Quiz")     
    private String title;
    
    @Schema(description = "Detailed description or instructions for the quiz", example = "This quiz covers basic Java concepts. You need to score at least 70% to pass.")
    private String description;

    @Schema(description = "Minimum score % to pass (default 60)", example = "70")
    private Integer passingScore;   

    @Schema(description = "If true, the quiz is published and available to students", example = "true")  
    private boolean published;
    
    @Schema(description = "Seconds allowed to complete the quiz. null = no limit", example = "300")
    private Integer timeLimitSeconds;
    
    @Schema(description = "List of questions in the quiz")
    private List<QuizQuestion> questions;
    
    @Schema(description = "Timestamp when the quiz was created", example = "2023-01-01T00:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "Timestamp when the quiz was last updated", example = "2023-01-01T00:00:00")
    private LocalDateTime updatedAt;
}