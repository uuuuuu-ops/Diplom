package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.QuizQuestion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
@Schema(description = "A request to update quiz details, including title, description, and other properties")
@Data
public class UpdateQuizRequest {
    @Schema(description = "The title of the quiz, which is required for the update")
    private String title;
    @Schema(description = "The description of the quiz, which is required for the update")
    private String description;
    @Schema(description = "The passing score for the quiz, which is required for the update")
    private Integer passingScore;
    @Schema(description = "The time limit for the quiz, which is required for the update")
    private Integer timeLimitSeconds;
    @Schema(description = "The list of questions for the quiz, which is required for the update")
    private List<QuizQuestion> questions;
    @Schema(description = "The publication status of the quiz, which is required for the update")
    private Boolean published;
}