package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "teacher_quiz_questions")
public class TeacherQuizQuestion {

    @Id
    private String id;

    private String topic;          // "Java", "Python", "Data Science" и т.д.
    private String question;       // текст вопроса
    private List<String> options;  // 4 варианта ответа
    private int correctIndex;      // индекс правильного (0-3)
    private String explanation;    // объяснение правильного ответа
}