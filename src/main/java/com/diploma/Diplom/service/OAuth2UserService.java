package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final GitHubEmailFetcher gitHubEmailFetcher;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        processUser(userRequest, oAuth2User.getAttributes());
        return oAuth2User;
    }

    public User processUser(OAuth2UserRequest userRequest, Map<String, Object> attributes) {
        String provider = userRequest.getClientRegistration().getRegistrationId();

        String email = extractEmail(provider, attributes);
        String name  = extractName(provider, attributes);

        String resolvedEmail;
        if ("github".equals(provider) && email == null) {
            String accessToken = userRequest.getAccessToken().getTokenValue();
            resolvedEmail = gitHubEmailFetcher.fetchPrimaryEmail(accessToken);
        } else {
            resolvedEmail = email;
        }

        if (resolvedEmail == null) {
            throw new BadRequestException("Email not provided by OAuth2 provider: " + provider);
        }

        User user = userRepository.findByEmail(resolvedEmail).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(resolvedEmail);
            newUser.setName(name);
            newUser.setRole(Role.STUDENT);
            newUser.setEnabled(true);
            newUser.setPassword("");
            newUser.setCreatedAt(LocalDateTime.now());
            log.info("New OAuth2 user registered: {} via {}", resolvedEmail, provider);
            return userRepository.save(newUser);
        });

        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            userRepository.save(user);
        }

        return user;
    }

    private String extractEmail(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case "google" -> (String) attrs.get("email");
            case "github" -> (String) attrs.get("email");
            default -> null;
        };
    }

    private String extractName(String provider, Map<String, Object> attrs) {
        return switch (provider) {
            case "google" -> (String) attrs.get("name");
            case "github" -> (String) attrs.getOrDefault("name", attrs.get("login"));
            default -> null;
        };
    }
}