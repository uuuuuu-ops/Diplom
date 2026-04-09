package com.diploma.Diplom.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "A verification code for email verification or password reset")
@Data
@Document(collection = "verification_codes")
public class VerificationCode {

    @Schema(description = "mongoDB ObjectId")
    @Id
    private String id;

    @Schema(description = "The email address associated with this verification code")
    private String email;

    @Schema(description = "Code used for verifying the user's email or resetting the password")
    private String code;

    @Schema(description = "The timestamp when this verification code expires")
    private LocalDateTime expiresAt;

}