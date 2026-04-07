package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.PaymentException;
import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class PaypalSubscriptionService {

    private final SubscriptionService subscriptionService;
    private final SecurityUtils securityUtils;
    private final PaypalTokenCache tokenCache;
    private final RestTemplate restTemplate = new RestTemplate();

    private final String paypalPlanId;
    private final String paypalBaseUrl;

    public PaypalSubscriptionService(SubscriptionService subscriptionService,
                                     SecurityUtils securityUtils,
                                     PaypalTokenCache tokenCache,
                                     @Value("${paypal.subscription.plan-id}") String paypalPlanId,
                                     @Value("${paypal.base-url}") String paypalBaseUrl) {
        this.subscriptionService = subscriptionService;
        this.securityUtils = securityUtils;
        this.tokenCache = tokenCache;
        this.paypalPlanId = paypalPlanId;
        this.paypalBaseUrl = paypalBaseUrl;
    }

    public String getPlanId() {
        return paypalPlanId;
    }

    public Subscription savePendingSubscription(String paypalSubscriptionId, String planCode) {
        String userId = securityUtils.getCurrentUserId();
        return subscriptionService.createPendingSubscription(
                userId, planCode, paypalPlanId, paypalSubscriptionId
        );
    }

    public Subscription confirmSubscription(String paypalSubscriptionId) {
        verifyActiveOnPaypal(paypalSubscriptionId);
        return subscriptionService.activateSubscription(paypalSubscriptionId);
    }

    public Subscription cancelSubscription(String paypalSubscriptionId) {
        return subscriptionService.cancelSubscription(paypalSubscriptionId);
    }

    private void verifyActiveOnPaypal(String paypalSubscriptionId) {
        String accessToken = tokenCache.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<Map> response = restTemplate.exchange(
                paypalBaseUrl + "/v1/billing/subscriptions/" + paypalSubscriptionId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body == null) {
            throw new PaymentException("Empty response from PayPal for subscription: " + paypalSubscriptionId);
        }

        String status = (String) body.get("status");
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new PaymentException(
                    "Subscription is not active on PayPal. Current status: " + status
            );
        }
    }
}