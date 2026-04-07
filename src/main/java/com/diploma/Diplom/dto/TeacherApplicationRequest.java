package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Schema(description = "A request to apply for a teacher position, including personal information and qualifications")
@Data
public class TeacherApplicationRequest {
    @Schema(description = "The ID of the user applying for the teacher position")
    @NotBlank(message = "UserId is required")
    private String userId;

    @Schema(description = "The full name of the applicant, which is required for the application")
    @NotBlank(message = "Full name is required")
    private String fullName;

    @Schema(description = "The email address of the applicant, which is required for the application")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "The specialization of the applicant, which is required for the application")
    @NotBlank(message = "Specialization is required")
    private String specialization;

    @Schema(description = "The number of years of teaching experience the applicant has, which must be a non-negative integer")
    @Min(value = 0, message = "Experience cannot be negative")
    private int yearsOfExperience;

}