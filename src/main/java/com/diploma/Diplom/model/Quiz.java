package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "quizzes")
public class Quiz {

    @Id
    private String id;

    private String lessonId;      
    private String title;
    private String description;
    private Integer passingScore; 
    private boolean published;

    private List<QuizQuestion> questions;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}