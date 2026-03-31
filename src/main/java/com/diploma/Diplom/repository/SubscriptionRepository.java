package com.diploma.Diplom.repository;

import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends MongoRepository<Subscription, String> {

    Optional<Subscription> findByPaypalSubscriptionId(String paypalSubscriptionId);

    Optional<Subscription> findByUserIdAndStatus(String userId, SubscriptionStatus status);

    boolean existsByUserIdAndStatus(String userId, SubscriptionStatus status);

    List<Subscription> findByUserId(String userId);
}