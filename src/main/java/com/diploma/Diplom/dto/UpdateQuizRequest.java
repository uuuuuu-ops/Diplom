package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.QuizQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "A request to update quiz details, including title, description, and other properties")
@Data
public class UpdateQuizRequest {

    @Schema(description = "The title of the quiz, which is required for the update")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Schema(description = "The description of the quiz, which is required for the update")
    @Size(min = 5, max = 2000, message = "Description must be between 5 and 2000 characters")
    private String description;

    @Schema(description = "The passing score for the quiz, which is required for the update")
    @NotNull(message = "Passing score is required")
    @Min(value = 0, message = "Passing score cannot be less than 0")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    private Integer passingScore;

    @Schema(description = "The time limit for the quiz, which is required for the update")
    @Min(value = 1, message = "Time limit must be at least 1 second")
    private Integer timeLimitSeconds;

    @Schema(description = "The list of questions for the quiz, which is required for the update")
    @NotEmpty(message = "Questions are required")
    @Valid
    private List<QuizQuestion> questions;

    @Schema(description = "The publication status of the quiz, which is required for the update")
    @NotNull(message = "Published flag is required")
    private Boolean published;
}