package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "A request to update lesson details, including title, description, order index, duration, lecture text, and publication status")
@Data
public class UpdateLessonRequest {

    @Schema(description = "The title of the lesson, which is required for the update")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @Schema(description = "The description of the lesson, which is required for the update")
    @Size(min = 5, max = 2000, message = "Description must be between 5 and 2000 characters")
    private String description;

    @Schema(description = "The order index of the lesson, which is required for the update")
    @Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex;

    @Schema(description = "The duration of the lesson, which is required for the update")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @Schema(description = "The lecture text of the lesson, which is required for the update")
    @Size(min = 10, max = 20000, message = "Lecture text must be between 10 and 20000 characters")
    private String lectureText;

    @Schema(description = "The publication status of the lesson, which is required for the update")
    @NotNull(message = "Published flag is required")
    private Boolean published;

    @Schema(description = "The quiz requirement of the lesson, which is required for the update")
    @NotNull(message = "Quiz required flag is required")
    private Boolean quizRequired;
}