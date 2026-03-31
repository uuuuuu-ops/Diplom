package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Subscription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaypalSubscriptionService {

    private final SubscriptionService subscriptionService;

    @Value("${paypal.subscription.plan-id}")
    private String paypalPlanId;

    public PaypalSubscriptionService(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    public String getPlanId() {
        return paypalPlanId;
    }

    public Subscription savePendingSubscription(String paypalSubscriptionId, String planCode) {
        String userId = subscriptionService.getCurrentUserId();
        return subscriptionService.createPendingSubscription(
                userId,
                planCode,
                paypalPlanId,
                paypalSubscriptionId
        );
    }

    public Subscription confirmSubscription(String paypalSubscriptionId) {
        return subscriptionService.activateSubscription(paypalSubscriptionId);
    }

    public Subscription cancelSubscription(String paypalSubscriptionId) {
        return subscriptionService.cancelSubscription(paypalSubscriptionId);
    }
}