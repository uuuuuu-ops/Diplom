package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateSubscriptionRequest;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import com.diploma.Diplom.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    public Subscription createSubscription(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    public List<Subscription> getSubscriptionsByUser(String userId) {
        return subscriptionRepository.findByUserId(userId);
    }


    public Subscription createPendingSubscription(CreateSubscriptionRequest request) {
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setType(request.getType());
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setActive(false);
        subscription.setAutoRenew(false);

        return subscriptionRepository.save(subscription);
    }
}