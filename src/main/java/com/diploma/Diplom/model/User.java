package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A user of the platform")
@Data
@Document(collection = "users")
public class User {

    @Schema(description = "Unique identifier of the user")
    @Id
    private String id;

    @Schema(description = "Full name of the user")
    private String name;

    @Schema(description = "Email address of the user, used for login and notifications")
    private String email;

    @Schema(description = "Hashed password — never returned in API responses", accessMode = Schema.AccessMode.WRITE_ONLY)
    @JsonIgnore
    private String password;

    @Schema(description = "Role of the user: STUDENT or TEACHER")
    private Role role;

    @Schema(description = "PayPal subscription ID, set when the user subscribes to a plan")
    private String subscriptionId;

    @Schema(description = "Date and time when the user registered on the platform")
    private LocalDateTime createdAt;

    @Schema(description = "Whether the user's email has been verified")
    private boolean enabled;

    @Schema(description = "Whether the teacher has been approved by an admin after passing the qualification quiz")
    private boolean teacherApproved;

    @Schema(description = "Profile Image Url")
    private String profileImageUrl;

    @Schema(description = "Age of the user")
    private Integer age;
}