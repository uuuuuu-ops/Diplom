package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {
    List<Subscription> findByUserId(String userId);
    Optional<Subscription> findFirstByUserIdAndActiveTrue(String userId);
    Optional<Subscription> findFirstByUserIdAndStatus(String userId, SubscriptionStatus status);
}