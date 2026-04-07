package com.diploma.Diplom.dto;

import lombok.Data;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema(description = "A request to submit answers for a quiz")
@Data
public class SubmitQuizRequest {
    @Schema(description = "The ID of the quiz for which answers are being submitted")
    private String quizId;
    @Schema(description = "A list of the user's answers to the quiz questions, where each answer is represented by the index of the selected option (0-3)")
    private List<Integer> answers;
}