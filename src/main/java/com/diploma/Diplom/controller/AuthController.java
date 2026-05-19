package com.diploma.Diplom.controller;

import com.diploma.Diplom.auth.AuthRequest;
import com.diploma.Diplom.auth.AuthResponse;
import com.diploma.Diplom.auth.AuthService;
import com.diploma.Diplom.auth.RegisterRequest;
import com.diploma.Diplom.auth.VerifyRequest;
import com.diploma.Diplom.exception.TooManyRequestsException;
import com.diploma.Diplom.service.JwtBlacklistService;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.RateLimiterService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Register, verify email, log in, and log out")
@SecurityRequirements
public class AuthController {

    private final AuthService authService;
    private final RateLimiterService rateLimiter;
    private final JwtBlacklistService blacklistService;
    private final JwtService jwtService;

    public AuthController(AuthService authService,
                          RateLimiterService rateLimiter,
                          JwtBlacklistService blacklistService,
                          JwtService jwtService) {
        this.authService = authService;
        this.rateLimiter = rateLimiter;
        this.blacklistService = blacklistService;
        this.jwtService = jwtService;
    }

    @Operation(
        summary = "Register a new user",
        description = "Creates a STUDENT or TEACHER account and sends a verification email.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Account created — check email for code",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or email already taken",
                content = @Content),
            @ApiResponse(responseCode = "429", description = "Too many requests", content = @Content)
        }
    )
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request,
                                 HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("register", getClientIp(httpReq), 10, Duration.ofHours(1))) {
            throw new TooManyRequestsException("Too many registration attempts. Try again later.");
        }
        return authService.register(request);
    }

    @Operation(
        summary = "Verify email with the 6-digit code",
        description = "Submit the code that was emailed after registration.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Email verified successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too many attempts", content = @Content)
        }
    )
    @PostMapping("/verify")
    public String verify(@RequestBody VerifyRequest request, HttpServletRequest httpReq) {
        if (!rateLimiter.isAllowed("verify", request.getEmail(), 10, Duration.ofMinutes(15))) {
            throw new TooManyRequestsException("Too many verification attempts. Try again in 15 minutes.");
        }
        return authService.verify(request);
    }

    @Operation(
        summary = "Log in and receive a JWT token",
        description = "Returns a JWT bearer token.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Bad credentials", content = @Content),
            @ApiResponse(responseCode = "429", description = "Too many login attempts", content = @Content)
        }
    )
    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request, HttpServletRequest httpReq) {
        String ip = getClientIp(httpReq);
        if (!rateLimiter.isAllowed("login:ip", ip, 20, Duration.ofMinutes(1))) {
            throw new TooManyRequestsException("Too many login attempts from this IP. Try again later.");
        }
        if (!rateLimiter.isAllowed("login:email", request.getEmail(), 5, Duration.ofMinutes(5))) {
            throw new TooManyRequestsException("Too many login attempts for this account. Try again in 5 minutes.");
        }
        return authService.login(request);
    }

    @Operation(
        summary = "Log out (invalidate JWT)",
        description = "Blacklists the current JWT so it cannot be used again, even before it expires.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "No token provided", content = @Content)
        }
    )
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public Map<String, String> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                String jti = jwtService.extractJti(token);
                blacklistService.blacklist(jti, jwtService.getRemainingTtl(token));
            } catch (Exception ignored) {
            }
        }
        return Map.of("message", "Logged out successfully");
    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            String[] parts = forwarded.split(",");
            return parts[parts.length - 1].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}