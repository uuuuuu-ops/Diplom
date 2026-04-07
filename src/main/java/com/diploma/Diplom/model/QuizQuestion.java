package com.diploma.Diplom.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Data
@Schema(description = "A quiz question for a course on the platform")
public class QuizQuestion {
    @Schema(description = "The question text to be displayed to students")
    private String question;
    @Schema(description = "The options for the quiz question, typically 4 choices")
    private List<String> options;
    @Schema(description = "The index of the correct answer within the options list (0-based)", example = "2")
    private Integer correctAnswerIndex;

    
}