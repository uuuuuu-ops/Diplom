package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
@Schema(description = "A question in the teacher qualification quiz")
@Data
@Document(collection = "teacher_quiz_questions")
public class TeacherQuizQuestion {

    @Schema(description = "A question in the teacher qualification quiz")
    @Id
    private String id;

    @Schema(description = "The topic or specialization related to the quiz question, e.g. 'Java', 'Python', 'Data Science'")
    private String topic;        

    @Schema(description = "The question text")
    private String question;       

    @Schema(description = "The list of answer options")
    private List<String> options;  

    @Schema(description = "The index of the correct answer (0-3)")
    private int correctIndex;      
    
    @Schema(description = "The explanation for the correct answer, shown to teachers after they answer the question")
    private String explanation;    
}