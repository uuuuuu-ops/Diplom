package com.diploma.Diplom.service;

import com.diploma.Diplom.config.PaypalProperties;
import com.diploma.Diplom.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Primary          
@Component
@RequiredArgsConstructor
public class PaypalTokenRedisCache {

    private static final String REDIS_KEY = "paypal:access_token";

    private final PaypalProperties paypalProperties;
    private final RedisTemplate<String, String> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public String getAccessToken() {
        String cached = redisTemplate.opsForValue().get(REDIS_KEY);
        if (cached != null) {
            return cached;
        }
        return fetchAndCache();
    }

    private String fetchAndCache() {
        log.debug("Fetching new PayPal access token from API");

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(paypalProperties.getClientId(), paypalProperties.getClientSecret());
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                paypalProperties.getBaseUrl() + "/v1/oauth2/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new PaymentException("Failed to get PayPal access token");
        }

        String token = (String) responseBody.get("access_token");
        int expiresIn = (Integer) responseBody.getOrDefault("expires_in", 3600);
        // Сохраняем с TTL на 60 секунд меньше реального срока
        redisTemplate.opsForValue().set(REDIS_KEY, token, Duration.ofSeconds(expiresIn - 60));

        log.debug("PayPal token cached in Redis, TTL = {} sec", expiresIn - 60);
        return token;
    }
}