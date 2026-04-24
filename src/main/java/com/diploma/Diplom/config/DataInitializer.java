// config/DataInitializer.java
package com.diploma.Diplom.config;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:}")
    private String adminPassword;

    @Value("${app.admin.email:admin@lms.com}")
    private String adminEmail;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException(
                "APP_ADMIN_PASSWORD environment variable is not set. " +
                "Set it to a strong password before starting the application."
            );
        }

        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setName("Admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            admin.setTeacherApproved(false);
            admin.setCreatedAt(LocalDateTime.now());
            userRepository.save(admin);
            log.info("Default admin account created: {}", adminEmail);
        }
    }
}