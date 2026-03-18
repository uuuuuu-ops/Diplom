package com.diploma.Diplom.model;

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

    private String question;

    private List<String> options;

    private String correctAnswer;
}