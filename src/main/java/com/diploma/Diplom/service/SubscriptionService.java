package com.diploma.Diplom.service;

import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import com.diploma.Diplom.repository.SubscriptionRepository;
import com.diploma.Diplom.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    public String getCurrentUserId() {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
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
        Subscription subscription = subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        return subscriptionRepository.save(subscription);
    }

    public Subscription cancelSubscription(String paypalSubscriptionId) {
        Subscription subscription = subscriptionRepository.findByPaypalSubscriptionId(paypalSubscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());

        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getMySubscriptions() {
        return subscriptionRepository.findByUserId(getCurrentUserId());
    }
}