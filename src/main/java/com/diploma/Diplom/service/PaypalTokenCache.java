package com.diploma.Diplom.service;

import com.diploma.Diplom.config.PaypalProperties;
import com.diploma.Diplom.exception.PaymentException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaypalTokenCache {

    private final PaypalProperties paypalProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    private volatile String cachedToken;
    private volatile Instant tokenExpiresAt = Instant.MIN;

    private final ReentrantLock lock = new ReentrantLock();

    public String getAccessToken() {
        if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedToken;
        }

        lock.lock();
        try {
            if (cachedToken != null && Instant.now().isBefore(tokenExpiresAt)) {
                return cachedToken;
            }
            return fetchNewToken();
        } finally {
            lock.unlock();
        }
    }

    private String fetchNewToken() {
        log.debug("Fetching new PayPal access token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(paypalProperties.getClientId(), paypalProperties.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            paypalProperties.getBaseUrl() + "/v1/oauth2/token",
            HttpMethod.POST,
            entity,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new PaymentException("Failed to get PayPal access token");
        }

        cachedToken = (String) responseBody.get("access_token");

        int expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600);
        tokenExpiresAt = Instant.now().plusSeconds(expiresIn - 60);

        log.debug("PayPal access token refreshed, expires in {} seconds", expiresIn - 60);
        return cachedToken;
    }
}
