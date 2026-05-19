package com.diploma.Diplom.auth;

import com.diploma.Diplom.exception.*;
import com.diploma.Diplom.messaging.EmailProducer;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.security.JwtService;
import com.diploma.Diplom.service.VerificationCodeRedisService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerificationCodeRedisService verificationCodeRedisService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailProducer emailProducer;

    private final SecureRandom secureRandom = new SecureRandom();

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }

        if (request.getRole() == Role.ADMIN) {
            throw new BadRequestException("Invalid role");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setTeacherApproved(false);
        user.setEnabled(false);

        userRepository.save(user);

        String code = generateCode();
        verificationCodeRedisService.save(request.getEmail(), code);
        emailProducer.sendVerificationEmail(request.getEmail(), code);

        return new AuthResponse("Verification code sent to email");
    }

    public String verify(VerifyRequest request) {
        boolean valid = verificationCodeRedisService.verify(request.getEmail(), request.getCode());
        if(!valid){
            throw new UnauthorizedException("Invalid verification Code");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepository.save(user);

        verificationCodeRedisService.delete(request.getEmail());
        return "Account verified successfully";
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new ForbiddenException("Account is not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                user.getRole(),
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