package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.PayPalLink;
import com.diploma.Diplom.dto.PayPalSubscriptionResponse;
import com.diploma.Diplom.exception.BadRequestException;
import com.diploma.Diplom.exception.PaymentException;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
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

    @Value("${app.base-url}")
    private String appBaseUrl;

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

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/v1/oauth2/token",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new PaymentException("Failed to get PayPal access token");
        }
        return (String) responseBody.get("access_token");
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
        // Use configurable base URL, not hardcoded localhost
        context.put("return_url", appBaseUrl + "/subscriptions/paypal/confirm");
        context.put("cancel_url", appBaseUrl + "/subscriptions/paypal/cancel");
        body.put("application_context", context);

        ResponseEntity<PayPalSubscriptionResponse> response = restTemplate.exchange(
                baseUrl + "/v1/billing/subscriptions",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                PayPalSubscriptionResponse.class
        );

        PayPalSubscriptionResponse responseBody = response.getBody();
        if (responseBody == null) {
            throw new PaymentException("Empty response from PayPal when creating subscription");
        }

        String subscriptionId = responseBody.getId();
        List<PayPalLink> links = responseBody.getLinks();

        String approvalUrl = links.stream()
                .filter(l -> "approve".equals(l.getRel()))
                .map(PayPalLink::getHref)
                .findFirst()
                .orElseThrow(() -> new PaymentException("Approval link not found in PayPal response"));

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

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl + "/v1/billing/subscriptions/" + subscriptionId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new PaymentException("Empty response from PayPal when verifying subscription");
        }

        String status = (String) responseBody.get("status");
        if (!"ACTIVE".equals(status)) {
            throw new BadRequestException("Subscription not active in PayPal: " + status);
        }

        return subscriptionService.activateSubscription(subscriptionId);
    }
}