package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.QuizQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Schema(description = "Request to create a new quiz for a lesson")
@Data
public class CreateQuizRequest {
    @Schema(description = "The title of the quiz")
    private String title;
    @Schema(description = "The description of the quiz, including instructions for students")
    private String description;
    @Schema(description = "The minimum score required to pass the quiz, as a percentage (e.g. 70 for 70%)")
    private Integer passingScore;
    @Schema(description = "The time limit for the quiz in seconds. If null, there is no time limit.")
    private Integer timeLimitSeconds;
    @Schema(description = "The questions for the quiz")
    private List<QuizQuestion> questions;
}