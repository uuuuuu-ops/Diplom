package com.diploma.Diplom.dto;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "User data returned in API responses — password is never included")
@Data
public class UserResponse {

    @Schema(description = "Unique identifier")
    private String id;

    @Schema(description = "Full name")
    private String name;

    @Schema(description = "Email address")
    private String email;

    @Schema(description = "Role: STUDENT, TEACHER, or ADMIN")
    private Role role;

    @Schema(description = "Whether the account email has been verified")
    private boolean enabled;

    @Schema(description = "Whether the teacher has been approved by an admin")
    private boolean teacherApproved;

    @Schema(description = "Profile image URL")
    private String profileImageUrl;

    @Schema(description = "Registration date")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setEnabled(user.isEnabled());
        dto.setTeacherApproved(user.isTeacherApproved());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}