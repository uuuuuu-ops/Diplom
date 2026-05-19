package com.diploma.Diplom.dto;

import lombok.Data;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
@Schema(description = "A request to submit answers for a quiz")
@Data
public class SubmitQuizRequest {

    @NotBlank(message = "Quiz ID is required")
    @Schema(description = "The ID of the quiz for which answers are being submitted")
    private String quizId;

    @Schema(description = "A list of the user's answers to the quiz questions, where each answer is represented by the index of the selected option (0-3)")
    @NotEmpty(message = "Answers are required")
    private List<@NotNull(message = "Answer cannot be null") Integer> answers;
}