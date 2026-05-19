package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Role;
import com.diploma.Diplom.model.User;
import com.diploma.Diplom.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    GitHubEmailFetcher gitHubEmailFetcher;

    @InjectMocks
    OAuth2UserService oAuth2UserService;

    @Test
    void google_newUser_createsAndReturns() {
        var attrs = Map.<String, Object>of("email", "user@gmail.com", "name", "Test User");
        var request = makeRequest("google", attrs);

        when(userRepository.findByEmail("user@gmail.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = oAuth2UserService.processUser(request, attrs);

        assertThat(result.getEmail()).isEqualTo("user@gmail.com");
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getRole()).isEqualTo(Role.STUDENT);
        assertThat(result.isEnabled()).isTrue();
    }

    private OAuth2UserRequest makeRequest(String provider, Map<String, Object> attributes) {
        ClientRegistration registration = ClientRegistration
                .withRegistrationId(provider)
                .clientId("client-id")
                .clientSecret("secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .authorizationUri("http://provider/auth")
                .tokenUri("http://provider/token")
                .userInfoUri("http://provider/userinfo")
                .userNameAttributeName(attributes.containsKey("email") ? "email" : "login")
                .build();

        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "test-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        return new OAuth2UserRequest(registration, token);
    }
}