package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "A request to submit a course rating and review")
@Data
public class RatingRequest {
    @NotBlank(message = "Course ID is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot be more than 5")
    @Schema(description = "The rating for the course (e.g., 1-5 stars)")
    private int rating;

    @NotBlank(message = "Course ID is required")
    @Schema(description = "The review text for the course")
    @Size(max = 1000, message = "Review cannot exceed 1000 characters")
    private String review;
}