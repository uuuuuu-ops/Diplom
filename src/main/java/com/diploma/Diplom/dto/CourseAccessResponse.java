package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
@Schema(description = "Response indicating whether the user has access to the course")
@Data
@AllArgsConstructor
public class CourseAccessResponse {
    
    @Schema(description = "Indicates whether the user has access to the course")
    private boolean hasAccess;
}