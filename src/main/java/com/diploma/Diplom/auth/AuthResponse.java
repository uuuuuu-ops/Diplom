package com.diploma.Diplom.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.diploma.Diplom.model.Role;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Authentication response containing user tokens and information after login")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    @Schema(description = "The authentication token for the user, used for subsequent requests to protected endpoints")
    private String token;
    @Schema(description = "A message related to the authentication process")
    private String message;
    @Schema(description = "The role of the authenticated user, e.g., 'student', 'teacher', 'admin'")
    private Role role;
    @Schema(description = "Indicates whether the user has been approved by a teacher (if the user is a teacher applicant)")
    private Boolean teacherApproved;
    @Schema(description = "The email address of the authenticated user")
    private String email;
    @Schema(description = "The name of the authenticated user")
    private String name;

    public AuthResponse(String message) {
    this.message = message;
    }

    public AuthResponse(String token, Role role, Boolean teacherApproved, String email, String name) {
        this.token = token;
        this.role = role;
        this.teacherApproved = teacherApproved;
        this.email = email;
        this.name = name;
    }
}