package com.diploma.Diplom.auth;

import com.diploma.Diplom.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request containing user credentials for registration")
@Data
public class RegisterRequest {

    @Schema(description = "The name of the user registering for an account")
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Schema(description = "The email address of the user registering for an account")
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Schema(description = "Password — min 8 chars, must contain at least one letter and one digit")
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
        message = "Password must contain at least one letter and one digit"
    )
    private String password;

    @Schema(
        description = "Role chosen at registration: STUDENT or TEACHER only. ADMIN cannot be self-assigned.",
        allowableValues = {"STUDENT", "TEACHER"}
    )
    @NotNull(message = "Role is required")
    private Role role;
}