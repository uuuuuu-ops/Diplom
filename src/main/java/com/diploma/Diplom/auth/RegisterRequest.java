package com.diploma.Diplom.auth;

import com.diploma.Diplom.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request containing user credentials for registration")
@Data
public class RegisterRequest {
    @Schema(description = "The name of the user registering for an account")
    @NotBlank(message = "Name is required")
    private String name;
    
    @Schema(description = "The email address of the user registering for an account")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "The password for the user's account")
    @NotBlank(message = "Password is required")
    @Size(min = 5, message = "Password must be at least 5 characters")
    private String password;

    @Schema(description = "The role of the user, e.g., STUDENT, TEACHER, or ADMIN")
    private Role role;
}

