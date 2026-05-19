package com.diploma.Diplom.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
@Schema(description = "Authentication request containing user credentials for login")
@Data
public class AuthRequest {

    @Schema(description = "The email address of the user")
    private String email;
    @Schema(description = "The password of the user")
    private String password;


}