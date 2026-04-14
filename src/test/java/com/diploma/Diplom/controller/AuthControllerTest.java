package com.diploma.Diplom.controller;

import com.diploma.Diplom.auth.AuthRequest;
import com.diploma.Diplom.auth.AuthResponse;
import com.diploma.Diplom.auth.AuthService;
import com.diploma.Diplom.auth.RegisterRequest;
import com.diploma.Diplom.auth.VerifyRequest;
import com.diploma.Diplom.exception.ConflictException;
import com.diploma.Diplom.exception.GlobalExceptionHandler;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.exception.UnauthorizedException;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({GlobalExceptionHandler.class, TestSecurityConfig.class})
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthService authService;
    @MockitoBean JwtService jwtService;
    @MockitoBean UserRepository userRepository;

    // ─────────────────────── POST /auth/register ─────────────────────────

    @Test
    @DisplayName("POST /auth/register: успешная регистрация — 200 и сообщение")
    void register_success() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@test.com");
        req.setPassword("securePass1");

        AuthResponse response = new AuthResponse("Verification code sent to alice@test.com");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Verification code sent to alice@test.com"));
    }

    @Test
    @DisplayName("POST /auth/register: email уже занят — 409 Conflict")
    void register_emailConflict() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@test.com");
        req.setPassword("securePass1");

        when(authService.register(any())).thenThrow(new ConflictException("Email already in use"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    // ─────────────────────── POST /auth/verify ───────────────────────────

    @Test
    @DisplayName("POST /auth/verify: верный код — 200")
    void verify_success() throws Exception {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("123456");

        when(authService.verify(any(VerifyRequest.class))).thenReturn("Account verified successfully");

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("Account verified successfully"));
    }

    @Test
    @DisplayName("POST /auth/verify: неверный/просроченный код — 401")
    void verify_invalidCode() throws Exception {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("000000");

        when(authService.verify(any())).thenThrow(new UnauthorizedException("Invalid or expired code"));

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────── POST /auth/login ────────────────────────────

    @Test
    @DisplayName("POST /auth/login: успешный вход — 200 с токеном и ролью")
    void login_success() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@test.com");
        req.setPassword("securePass1");

        // исправлено: создаём AuthResponse с явно заданной ролью
        AuthResponse response = new AuthResponse("jwt-token-abc", Role.STUDENT, false, "alice@test.com", "Alice");

        when(authService.login(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-abc"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    @DisplayName("POST /auth/login: пользователь не найден — 404")
    void login_userNotFound() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("pass");

        when(authService.login(any())).thenThrow(new ResourceNotFoundException("User not found"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }
}