package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.QuizQuestion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Schema(description = "Request to create a new quiz for a lesson")
@Data
public class CreateQuizRequest {

    @Schema(description = "The title of the quiz")
    @NotBlank(message = "Quiz title is required")
    @Size(min = 3, max = 200, message = "Quiz title must be between 3 and 200 characters")
    private String title;

    @Schema(description = "The description of the quiz, including instructions for students")
    @NotBlank(message = "Quiz description is required")
    @Size(min = 5, max = 2000, message = "Quiz description must be between 5 and 2000 characters")
    private String description;

    @Schema(description = "The minimum score required to pass the quiz, as a percentage (e.g. 70 for 70%)")
    @Min(value = 0, message = "Passing score cannot be less than 0")
    @Max(value = 100, message = "Passing score cannot exceed 100")
    private Integer passingScore;

    @Schema(description = "The time limit for the quiz in seconds. If null, there is no time limit.")
    @Min(value = 1, message = "Time limit must be at least 1 second")
    private Integer timeLimitSeconds;

    @Schema(description = "The questions for the quiz")
    @NotEmpty(message = "Quiz must contain at least one question")
    @Valid
    private List<QuizQuestion> questions;
}