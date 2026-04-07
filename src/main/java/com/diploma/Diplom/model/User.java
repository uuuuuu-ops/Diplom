package com.diploma.Diplom.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "A user of the platform")
@Data
@Document(collection = "users")
public class User {
    @Schema(description = "A user of the platform")
    @Id
    private String id;

    @Schema(description = "The name of the user")
    private String name;
    @Schema(description = "The email address of the user")
    private String email;
    @Schema(description = "The password of the user (should be stored securely in a real application)")
    private String password;
    @Schema(description = "The role of the user, either STUDENT or TEACHER")
    private Role role; 
    @Schema(description = "The date and time when the user registered on the platform")
    private String subscriptionId;
    @Schema(description = "The date and time when the user registered on the platform")
    private LocalDateTime createdAt;
    @Schema(description = "The date and time when the user registered on the platform")
    private boolean enabled;
    @Schema(description = "Indicates whether the teacher has been approved by an admin after passing the qualification quiz")
    private boolean teacherApproved;


}
