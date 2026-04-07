package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A request to submit a course rating and review")
@Data
public class RatingRequest {
    @Schema(description = "The rating for the course (e.g., 1-5 stars)")
    private int rating;
    @Schema(description = "The review text for the course")
    private String review;
}