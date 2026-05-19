package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request to mark a lesson as completed by a student")
@Data
public class CompleteLessonRequest {
    
    @Schema(description = "The ID of the course to which the lesson belongs")
    @NotBlank(message = "Course ID is required")
    private String courseId;

    @Schema(description = "The ID of the lesson to mark as completed")
    @NotBlank(message = "Lesson ID is required")
    private String lessonId;
}