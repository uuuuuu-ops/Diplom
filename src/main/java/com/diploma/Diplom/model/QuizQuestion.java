package com.diploma.Diplom.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class QuizQuestion {

    @NotBlank(message = "Question text is required")
    @Size(min = 5, max = 500, message = "Question text must be between 5 and 500 characters")
    private String question;

    @NotEmpty(message = "Options are required")
    @Size(min = 2, max = 6, message = "Options must contain between 2 and 6 items")
    private List<@NotBlank(message = "Option cannot be blank") String> options;

    @NotNull(message = "Correct answer index is required")
    @Min(value = 0, message = "Correct answer index cannot be negative")
    @Max(value = 5, message = "Correct answer index is too large")
    private Integer correctAnswerIndex;
}