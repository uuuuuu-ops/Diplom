package com.diploma.Diplom.auth;

import com.diploma.Diplom.exception.*;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.model.VerificationCode;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.repository.VerificationCodeRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock VerificationCodeRepository verificationCodeRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock EmailService emailService;

    @InjectMocks
    AuthService authService;

    // ───────────────────────────── register ──────────────────────────────

    @Test
    @DisplayName("register: успешная регистрация — возвращает сообщение об отправке кода")
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Alice");
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("hashed");
        when(verificationCodeRepository.findByEmail("alice@test.com")).thenReturn(Optional.empty());

        AuthResponse response = authService.register(req);

        assertThat(response.getMessage()).contains("Verification code sent");

        // пользователь должен быть сохранён с role=STUDENT и enabled=false
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getRole()).isEqualTo(Role.STUDENT);
        assertThat(saved.isEnabled()).isFalse();
        assertThat(saved.getPassword()).isEqualTo("hashed");

        verify(emailService).sendVerificationEmail(eq("alice@test.com"), anyString());
    }

    @Test
    @DisplayName("register: email уже занят — выбрасывает ConflictException")
    void register_emailAlreadyExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("alice@test.com");
        req.setName("Alice");
        req.setPassword("secret");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(ConflictException.class);

        verify(emailService, never()).sendVerificationEmail(any(), any());
    }

    @Test
    @DisplayName("register: ошибка отправки email — откатывает пользователя и выбрасывает InternalServerException")
    void register_emailSendFails_rollback() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Bob");
        req.setEmail("bob@test.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("hashed");
        when(verificationCodeRepository.findByEmail("bob@test.com")).thenReturn(Optional.empty());
        doThrow(new RuntimeException("SMTP error"))
                .when(emailService).sendVerificationEmail(any(), any());

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(InternalServerException.class);

        // пользователь и код должны быть удалены при откате
        verify(userRepository).delete(any(User.class));
        verify(verificationCodeRepository).delete(any(VerificationCode.class));
    }

    // ───────────────────────────── verify ────────────────────────────────

    @Test
    @DisplayName("verify: верный код — включает аккаунт и удаляет код")
    void verify_success() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("123456");

        VerificationCode vc = new VerificationCode();
        vc.setEmail("alice@test.com");
        vc.setCode("123456");
        vc.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        User user = new User();
        user.setEmail("alice@test.com");
        user.setEnabled(false);

        when(verificationCodeRepository.findByEmailAndCode("alice@test.com", "123456"))
                .thenReturn(Optional.of(vc));
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));

        String result = authService.verify(req);

        assertThat(result).containsIgnoringCase("verified");
        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(verificationCodeRepository).delete(vc);
    }

    @Test
    @DisplayName("verify: неверный код — выбрасывает UnauthorizedException")
    void verify_invalidCode() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("000000");

        when(verificationCodeRepository.findByEmailAndCode("alice@test.com", "000000"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verify(req))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("verify: просроченный код — выбрасывает UnauthorizedException")
    void verify_expiredCode() {
        VerifyRequest req = new VerifyRequest();
        req.setEmail("alice@test.com");
        req.setCode("123456");

        VerificationCode vc = new VerificationCode();
        vc.setEmail("alice@test.com");
        vc.setCode("123456");
        vc.setExpiresAt(LocalDateTime.now().minusMinutes(1)); // уже истёк

        when(verificationCodeRepository.findByEmailAndCode("alice@test.com", "123456"))
                .thenReturn(Optional.of(vc));

        assertThatThrownBy(() -> authService.verify(req))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expired");
    }

    // ───────────────────────────── login ─────────────────────────────────

    @Test
    @DisplayName("login: успешный вход — возвращает токен и данные пользователя")
    void login_success() {
        AuthRequest req = new AuthRequest();
        req.setEmail("alice@test.com");
        req.setPassword("secret");

        User user = new User();
        user.setEmail("alice@test.com");
        user.setPassword("hashed");
        user.setEnabled(true);
        user.setRole(Role.STUDENT);
        user.setName("Alice");

        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("STUDENT");
        assertThat(response.getEmail()).isEqualTo("alice@test.com");
    }

    @Test
    @DisplayName("login: аккаунт не подтверждён — выбрасывает ForbiddenException")
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
    @DisplayName("login: неверный пароль — выбрасывает BadRequestException")
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
    @DisplayName("login: пользователь не найден — выбрасывает ResourceNotFoundException")
    void login_userNotFound() {
        AuthRequest req = new AuthRequest();
        req.setEmail("ghost@test.com");
        req.setPassword("pass");

        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}