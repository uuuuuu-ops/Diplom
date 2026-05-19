package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Schema(description = "Request containing the review comment for a teacher review")
@Data
public class TeacherReviewRequest {
    
    @Schema(description = "The review comment for the teacher review")
    @Size(min = 2, max = 100, message = "Review comment must be between 2 and 100 characters")
    private String reviewComment;

}