package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.messaging.SubscriptionProducer;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import com.diploma.Diplom.repository.SubscriptionRepository;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionProducer subscriptionProducer;
    private final SecurityUtils securityUtils;

    /**
     * Кешируем факт активной подписки на 3 минуты.
     * Инвалидируем при активации/отмене.
     */
    @Cacheable(value = "subscription", key = "#userId")
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

    @CacheEvict(value = "subscription", key = "#result.userId")
    public Subscription activateSubscription(String paypalSubscriptionId) {
        Subscription subscription = getByPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStartedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);

        // Async email уведомление об активации
        String email = userRepository.findById(saved.getUserId())
                .map(u -> u.getEmail())
                .orElse(null);
        if (email != null) {
            subscriptionProducer.sendSubscriptionEvent(
                    saved.getUserId(), email, "ACTIVATED", saved.getPlanCode());
        }

        return saved;
    }

    @CacheEvict(value = "subscription", key = "#result.userId")
    public Subscription cancelSubscription(String paypalSubscriptionId) {
        Subscription subscription = getByPaypalSubscriptionId(paypalSubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setEndedAt(LocalDateTime.now());
        subscription.setUpdatedAt(LocalDateTime.now());
        Subscription saved = subscriptionRepository.save(subscription);

        // Async email уведомление об отмене
        String email = userRepository.findById(saved.getUserId())
                .map(u -> u.getEmail())
                .orElse(null);
        if (email != null) {
            subscriptionProducer.sendSubscriptionEvent(
                    saved.getUserId(), email, "CANCELLED", saved.getPlanCode());
        }

        return saved;
    }

    public List<Subscription> getMySubscriptions() {
        return subscriptionRepository.findByUserId(securityUtils.getCurrentUserId());
    }

    public Subscription getByPaypalSubscriptionId(String id) {
        return subscriptionRepository.findByPaypalSubscriptionId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found"));
    }
}
