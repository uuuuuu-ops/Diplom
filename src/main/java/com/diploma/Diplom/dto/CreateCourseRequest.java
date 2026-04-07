package com.diploma.Diplom.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Schema(description = "Request to create a new course")
@Data
public class CreateCourseRequest {
    @Schema(description = "The title of the course")
    @NotBlank(message = "Title is required")
    private String title;
    @Schema(description = "The description of the course")
    @NotBlank(message = "Description is required")
    private String description;
    @Schema(description = "The category of the course")
    @NotBlank(message = "Category is required")
    private String category;
    @Schema(description = "The level of the course")
    private String level;
    @Schema(description = "Whether the course is published")
    private Boolean published;
    @Schema(description = "Whether the course is free")
    private boolean free;
    @Schema(description = "The price of the course in USD")
    private BigDecimal price;

}