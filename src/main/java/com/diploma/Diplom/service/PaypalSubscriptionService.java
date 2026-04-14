package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaypalSubscriptionService {

    private final RestTemplate restTemplate;
    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.plan-id}")
    private String planId;

    public PaypalSubscriptionService(RestTemplate restTemplate,
                                     SubscriptionService subscriptionService,
                                     SecurityUtils securityUtils) {
        this.restTemplate = restTemplate;
        this.subscriptionService = subscriptionService;
        this.securityUtils = securityUtils;
    }

    public String getPlanId() {
    return planId;
}

    public String getAccessToken() {

        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        HttpEntity<?> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v1/oauth2/token",
                request,
                Map.class
        );

        return (String) response.getBody().get("access_token");
    }

    public String createSubscriptionAndGetApprovalLink() {

        String token = getAccessToken(); 

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        Map<String, Object> body = new HashMap<>();
        body.put("plan_id", planId);

        Map<String, Object> context = new HashMap<>();
        context.put("brand_name", "Diploma App");
        context.put("return_url", "http://localhost:8080/subscriptions/paypal/confirm");
        context.put("cancel_url", "http://localhost:8080/subscriptions/paypal/cancel");

        body.put("application_context", context);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/v1/billing/subscriptions",
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        String subscriptionId = (String) responseBody.get("id");

        List<Map<String, String>> links =
                (List<Map<String, String>>) responseBody.get("links");

        String approvalUrl = links.stream()
                .filter(l -> "approve".equals(l.get("rel")))
                .map(l -> l.get("href"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Approval link not found"));

        subscriptionService.createPendingSubscription(
                securityUtils.getCurrentUserId(),
                "PRO",
                planId,
                subscriptionId
        );

        return approvalUrl;
    }

    public Subscription confirmSubscription(String subscriptionId) {

        String token = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/billing/subscriptions/" + subscriptionId,
                HttpMethod.GET,
                request,
                Map.class
        );

        String status = (String) response.getBody().get("status");

        if (!"ACTIVE".equals(status)) {
            throw new RuntimeException("Subscription not active: " + status);
        }

        return subscriptionService.activateSubscription(subscriptionId);
    }
}