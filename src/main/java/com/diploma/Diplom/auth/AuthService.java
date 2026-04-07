package com.diploma.Diplom.auth;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.model.VerificationCode;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.repository.VerificationCodeRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.EmailService;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRepository verificationCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       VerificationCodeRepository verificationCodeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.verificationCodeRepository = verificationCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User with this email already exists");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT);
        user.setEnabled(false);

        userRepository.save(user);

        String code = generateCode();

        // FIX: старый код удаляется перед созданием нового (при повторной регистрации)
        verificationCodeRepository.findByEmail(request.getEmail())
                .ifPresent(verificationCodeRepository::delete);

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(request.getEmail());
        verificationCode.setCode(code);
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        verificationCodeRepository.save(verificationCode);

        // FIX: если email не отправился — удаляем пользователя чтобы не осталось "мёртвых" записей
        try {
            emailService.sendVerificationEmail(request.getEmail(), code);
        } catch (Exception e) {
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
        Random random = new Random();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}