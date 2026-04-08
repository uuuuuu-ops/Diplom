package com.diploma.Diplom.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Request containing user credentials for verification, such as email and verification code")
@Data
public class VerifyRequest {
    @Schema(description = "The email address of the user verifying their account")
    private String email;
    @Schema(description = "The verification code sent to the user's email for account verification")
    private String code;

}