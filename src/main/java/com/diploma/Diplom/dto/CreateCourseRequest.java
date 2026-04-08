package com.diploma.Diplom.dto;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request to create a new course")
@Data
public class CreateCourseRequest {

    @Schema(description = "The title of the course")
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 120, message = "Title must be between 3 and 120 characters")
    private String title;

    @Schema(description = "The description of the course")
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 3000, message = "Description must be between 10 and 3000 characters")
    private String description;

    @Schema(description = "The category of the course")
    @NotBlank(message = "Category is required")
    @Size(max = 100, message = "Category must not exceed 100 characters")
    private String category;

    @Schema(description = "The level of the course")
    @NotBlank(message = "Level is required")
    private String level;

    @Schema(description = "Whether the course is published")
    private Boolean published;

    @Schema(description = "Whether the course is free")
    private Boolean free;

    @Schema(description = "The price of the course in USD")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;

}