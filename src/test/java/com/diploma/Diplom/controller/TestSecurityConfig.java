package com.diploma.Diplom.controller;

import com.diploma.Diplom.service.JwtBlacklistService;
import com.diploma.Diplom.service.RateLimiterService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    /**
     * JwtAuthenticationFilter is a @Component and gets picked up by @WebMvcTest.
     * It requires JwtBlacklistService in its constructor — provide a no-op mock.
     */
    @Bean
    @Primary
    public JwtBlacklistService jwtBlacklistService() {
        return mock(JwtBlacklistService.class);
    }

    /**
     * AuthController requires RateLimiterService — provide a permissive mock
     * that always returns true (all requests allowed in tests).
     */
    @Bean
    @Primary
    public RateLimiterService rateLimiterService() {
        RateLimiterService mock = mock(RateLimiterService.class);
        org.mockito.Mockito.when(mock.isAllowed(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(true);
        return mock;
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/courses/public").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}