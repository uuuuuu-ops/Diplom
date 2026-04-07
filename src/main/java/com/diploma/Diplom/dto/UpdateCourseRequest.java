package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "A request to update course details, including title, description, category, level, and publication status")
@Data
public class UpdateCourseRequest {
    @Schema(description = "The title of the course, which is required for the update")
    private String title;
    @Schema(description = "The description of the course, which is required for the update")
    private String description;
    @Schema(description = "The category of the course, which is required for the update")
    private String category;
    @Schema(description = "The level of the course, which is required for the update")
    private String level;
    @Schema(description = "The publication status of the course, which is required for the update")
    private Boolean published;
}