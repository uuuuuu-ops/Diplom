package com.diploma.Diplom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Schema(description = "A request to apply for a teacher position, including personal information and qualifications")
@Data
public class TeacherApplicationRequest {
    
    @Schema(description = "The ID of the user applying for the teacher position")
    @NotBlank(message = "UserId is required")
    private String userId;

    @Schema(description = "The full name of the applicant, which is required for the application")
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 20, message = "Full name must be between 2 and 20 characters")
    private String fullName;

    @Schema(description = "The email address of the applicant, which is required for the application")
    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "The specialization of the applicant, which is required for the application")
    @NotBlank(message = "Specialization is required")
    @Size(min = 2, max = 50, message = "Specialization must be between 2 and 50 characters")
    private String specialization;

    @Schema(description = "The number of years of teaching experience the applicant has, which must be a non-negative integer")
    @Min(value = 0, message = "Experience cannot be negative")
    private int yearsOfExperience;

}