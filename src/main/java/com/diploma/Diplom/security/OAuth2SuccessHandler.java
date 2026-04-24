package com.diploma.Diplom.security;


import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Value("${oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found after OAuth2: " + email));

        String token = jwtService.generateToken(user);

        // Редирект на фронтенд с JWT в параметре
        String targetUrl = redirectUri + "?token=" + token
                + "&role=" + user.getRole().name()
                + "&name=" + java.net.URLEncoder.encode(user.getName(), "UTF-8");

        log.info("OAuth2 login success for: {}", email);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}