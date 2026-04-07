package com.diploma.Diplom.service;

import com.diploma.Diplom.config.PaypalProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Component
public class PaypalTokenCache {

    private final PaypalProperties paypalProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private Instant tokenExpiresAt = Instant.MIN;

    public PaypalTokenCache(PaypalProperties paypalProperties) {
        this.paypalProperties = paypalProperties;
    }

    public String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }
        return fetchNewToken();
    }

    private String fetchNewToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(paypalProperties.getClientId(), paypalProperties.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                paypalProperties.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new RuntimeException("Failed to get PayPal access token");
        }

        cachedToken = (String) responseBody.get("access_token");

        // expires_in приходит в секундах, вычитаем 60 сек для запаса
        int expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600);
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60);

        return cachedToken;
    }
}