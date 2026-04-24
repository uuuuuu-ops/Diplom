package com.diploma.Diplom.auth;

import com.diploma.Diplom.exception.*;
import com.diploma.Diplom.messaging.EmailProducer;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.VerificationCodeRedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock VerificationCodeRedisService verificationCodeRedisService;
    @Mock EmailProducer emailProducer;

    @InjectMocks
    AuthService authService;

    // ───────────────────────── register ──────────────────────────────────

    @Test
    @DisplayName("register: успешная регистрация — возвращает сообщение об отправке кода")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");
        req.setRole(Role.STUDENT);

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        AuthResponse response = authService.register(req);

        assertThat(response.getMessage()).contains("Verification code sent");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.getPassword()).isEqualTo("hashed");

        verify(verificationCodeRedisService).save(eq("alice@test.com"), anyString());
        verify(emailProducer).sendVerificationEmail(eq("alice@test.com"), anyString());
    }

    @Test
    @DisplayName("register: email уже занят — ConflictException, пользователь не сохраняется")
    void register_emailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@test.com");
        req.setName("Alice");
        req.setPassword("secret");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class);

        verify(userRepository, never()).save(any());
        verify(emailProducer, never()).sendVerificationEmail(any(), any());
    }


    // ───────────────────────── verify ────────────────────────────────────

    @Test
    @DisplayName("verify: верный код — включает аккаунт и удаляет код из Redis")
    void verify_success() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("123456");

        when(verificationCodeRedisService.verify("alice@test.com", "123456")).thenReturn(true);

        User user = new User();
        user.setEmail("alice@test.com");
        user.setEnabled(false);
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        String result = authService.verify(req);

        assertThat(result).containsIgnoringCase("verified");
        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(verificationCodeRedisService).delete("alice@test.com");
    }

    @Test
    @DisplayName("verify: неверный код — UnauthorizedException, аккаунт не активируется")
    void verify_invalidCode_throws() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("000000");

        when(verificationCodeRedisService.verify("alice@test.com", "000000")).thenReturn(false);

        assertThatThrownBy(() -> authService.verify(req))
                .isInstanceOf(UnauthorizedException.class);

        verify(userRepository, never()).save(any());
        verify(verificationCodeRedisService, never()).delete(any());
    }

    @Test
    @DisplayName("verify: верный код, пользователь не найден — ResourceNotFoundException")
    void verify_userNotFound_throws() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("ghost@test.com");
        req.setCode("123456");

        when(verificationCodeRedisService.verify("ghost@test.com", "123456")).thenReturn(true);
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verify(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ───────────────────────── login ─────────────────────────────────────

    @Test
    @DisplayName("login: успешный вход — возвращает токен и данные пользователя")
    void login_success() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        User user = new User();
        user.setEmail("alice@test.com");
        user.setPassword("hashed");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        user.setTeacherApproved(false);
        user.setName("Alice");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token-xyz");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token-xyz");
        assertThat(response.getRole()).isEqualTo(Role.STUDENT);
        assertThat(response.getEmail()).isEqualTo("alice@test.com");
        assertThat(response.getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("login: аккаунт не подтверждён — ForbiddenException")
    void login_accountNotEnabled() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        User user = new User();
        user.setEnabled(false);

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("login: неверный пароль — BadRequestException")
    void login_wrongPassword() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@test.com");
        req.setPassword("wrong");

        User user = new User();
        user.setEnabled(true);
        user.setPassword("hashed");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("login: пользователь не найден — ResourceNotFoundException")
    void login_userNotFound() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}