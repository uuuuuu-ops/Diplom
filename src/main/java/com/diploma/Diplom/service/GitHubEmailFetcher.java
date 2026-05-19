// service/GitHubEmailFetcher.java
package com.diploma.Diplom.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GitHubEmailFetcher {

    private final RestTemplate restTemplate;

    public String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.set("Accept", "application/vnd.github+json");

        ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
            "https://api.github.com/user/emails",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            new org.springframework.core.ParameterizedTypeReference<List<Map<String, Object>>>() {}
        );

        if (response.getBody() == null) return null;

        return response.getBody().stream()
            .filter(m -> Boolean.TRUE.equals(m.get("primary"))
                      && Boolean.TRUE.equals(m.get("verified")))
            .map(m -> (String) m.get("email"))
            .findFirst()
            .orElse(null);
    }
}