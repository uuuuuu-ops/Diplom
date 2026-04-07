package com.diploma.Diplom.auth;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.model.VerificationCode;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.repository.VerificationCodeRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.STUDENT);
        user.setEnabled(false);

        userRepository.save(user);

        String code = generateCode();

        verificationCodeRepository.findByEmail(request.getEmail())
                .ifPresent(verificationCodeRepository::delete);

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(request.getEmail());
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(verificationCode);

        try {
            emailService.sendVerificationEmail(request.getEmail(), code);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", request.getEmail(), e.getMessage());
            userRepository.delete(user);
            verificationCodeRepository.delete(verificationCode);
            throw new RuntimeException("Failed to send verification email. Please try again.");
        }

        return new AuthResponse("Verification code sent to email");
    }

    public String verify(VerifyRequest request) {
        VerificationCode verificationCode = verificationCodeRepository
                .findByEmailAndCode(request.getEmail(), request.getCode())
                .orElseThrow(() -> new RuntimeException("Invalid verification code"));

        if (verificationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification code expired");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        verificationCodeRepository.delete(verificationCode);

        return "Account verified successfully";
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getRole().name(),
                user.isTeacherApproved(),
                user.getEmail(),
                user.getName()
        );
    }

    private String generateCode() {
        int number = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(number);
    }
}
