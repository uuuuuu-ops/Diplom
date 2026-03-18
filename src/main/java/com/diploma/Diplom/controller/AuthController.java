package com.diploma.Diplom.controller;

import com.diploma.Diplom.auth.AuthRequest;
import com.diploma.Diplom.auth.AuthResponse;
import com.diploma.Diplom.auth.AuthService;
import com.diploma.Diplom.auth.RegisterRequest;
import com.diploma.Diplom.auth.VerifyRequest;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify")
    public String verify(@RequestBody VerifyRequest request) {
        return authService.verify(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        return authService.login(request);
    }
}