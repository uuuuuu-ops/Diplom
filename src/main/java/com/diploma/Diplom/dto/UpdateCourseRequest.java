package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "A request to update course details, including title, description, category, level, and publication status")
@Data
public class UpdateCourseRequest {

    @Schema(description = "The title of the course, which is required for the update")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

    @Schema(description = "The description of the course, which is required for the update")
    @Size(min = 10, max = 3000, message = "Description must be between 10 and 3000 characters")
    private String description;

    @Schema(description = "The category of the course, which is required for the update")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Schema(description = "The level of the course, which is required for the update")
    @Size(max = 50, message = "Level must not exceed 50 characters")
    private String level;

    @Schema(description = "The publication status of the course, which is required for the update")
    @NotNull(message = "Published flag is required")
    private Boolean published;
}