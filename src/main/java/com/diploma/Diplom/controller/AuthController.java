package com.diploma.Diplom.controller;

import com.diploma.Diplom.auth.AuthRequest;
import com.diploma.Diplom.auth.AuthResponse;
import com.diploma.Diplom.auth.AuthService;
import com.diploma.Diplom.auth.RegisterRequest;
import com.diploma.Diplom.auth.VerifyRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register, verify email, and log in")
@SecurityRequirements   
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Register a new user",
        description = "Creates a STUDENT or TEACHER account and sends a verification email.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Account created — check email for code",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or email already taken",
                content = @Content)
        }
    )
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @Operation(
        summary = "Verify email with the 6-digit code",
        description = "Submit the code that was emailed after registration.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code",
                content = @Content)
        }
    )
    @PostMapping("/verify")
    public String verify(@RequestBody VerifyRequest request) {
        return authService.verify(request);
    }

    @Operation(
        summary = "Log in and receive a JWT token",
        description = "Returns a JWT bearer token. Paste it into the Authorize dialog at the top of this page.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials",
                content = @Content)
        }
    )
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}