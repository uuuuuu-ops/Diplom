package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import com.diploma.Diplom.repository.SubscriptionRepository;
import com.diploma.Diplom.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SecurityUtils securityUtils;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               SecurityUtils securityUtils) {
        this.subscriptionRepository = subscriptionRepository;
        this.securityUtils = securityUtils;
    }

    public boolean hasActiveSubscription(String userId) {
        return subscriptionRepository.existsByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    public Subscription createPendingSubscription(String userId,
                                                  String planCode,
                                                  String paypalPlanId,
                                                  String paypalSubscriptionId) {
        Subscription subscription = new Subscription();
        subscription.setUserId(userId);
        subscription.setPlanCode(planCode);
        subscription.setProvider("PAYPAL");
        subscription.setPaypalPlanId(paypalPlanId);
        subscription.setPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.APPROVAL_PENDING);
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        return subscriptionRepository.save(subscription);
    }

    public Subscription activateSubscription(String paypalSubscriptionId) {
        Subscription subscription = getByPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    public Subscription cancelSubscription(String paypalSubscriptionId) {
        Subscription subscription = getByPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getMySubscriptions() {
        return subscriptionRepository.findByUserId(securityUtils.getCurrentUserId());
    }

    public Subscription getByPaypalSubscriptionId(String id) {
        return subscriptionRepository.findByPaypalSubscriptionId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }
}