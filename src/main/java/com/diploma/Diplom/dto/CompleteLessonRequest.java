package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Request to mark a lesson as completed by a student")
@Data
public class CompleteLessonRequest {
    
    @Schema(description = "The ID of the course to which the lesson belongs")
    private String courseId;
    @Schema(description = "The ID of the lesson to mark as completed")
    private String lessonId;
}