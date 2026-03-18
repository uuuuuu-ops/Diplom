package com.diploma.Diplom.security;

import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        System.out.println("=== JWT FILTER START ===");
        System.out.println("URI: " + request.getRequestURI());
        System.out.println("Authorization header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.out.println("Token received: " + token);

            boolean valid = jwtService.validateToken(token);
            System.out.println("Token valid: " + valid);

            if (valid) {
                String email = jwtService.extractUsername(token);
                System.out.println("Email from token: " + email);

                User user = userRepository.findByEmail(email).orElse(null);
                System.out.println("User found: " + (user != null));

                if (user != null) {
                    System.out.println("User role: " + user.getRole());

                    List<SimpleGrantedAuthority> authorities = List.of(
                            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                    );

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    System.out.println("Authentication set in SecurityContext");
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}