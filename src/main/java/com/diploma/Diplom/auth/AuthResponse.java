package com.diploma.Diplom.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String message;
    private String role;
    private Boolean teacherApproved;
    private String email;
    private String name;

    public AuthResponse(String message) {
        this.message = message;
    }

    public AuthResponse(String token, String role, Boolean teacherApproved, String email, String name) {
        this.token = token;
        this.role = role;
        this.teacherApproved = teacherApproved;
        this.email = email;
        this.name = name;
    }
}