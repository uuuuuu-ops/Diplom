package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request to create a new lesson")
@Data
public class CreateLessonRequest {

    @Schema(description = "The title of the lesson")
    @NotBlank(message = "Lesson title is required")
    @Size(min = 3, max = 200, message = "Lesson title must be between 3 and 200 characters")
    private String title;

    @Schema(description = "The description of the lesson")
    @NotBlank(message = "Lesson description is required")
    @Size(min = 5, max = 2000, message = "Lesson description must be between 5 and 2000 characters")
    private String description;

    @Schema(description = "The index of the lesson in the course (starting from 0)")
    @NotNull(message = "Order index is required")
    @Min(value = 0, message = "Order index cannot be negative")
    private Integer orderIndex;

    @Schema(description = "The duration of the lesson in minutes")
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer duration;

    @Schema(description = "The text of the lesson lecture")
    @NotBlank(message = "Lecture text is required")
    @Size(min = 10, max = 20000, message = "Lecture text must be between 10 and 20000 characters")
    private String lectureText;

    @Schema(description = "Whether a quiz is required for the lesson")
    @NotNull(message = "Quiz required flag is required")
    private Boolean quizRequired = false;
}