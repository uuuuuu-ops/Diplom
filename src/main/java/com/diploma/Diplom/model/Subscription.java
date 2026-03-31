package com.diploma.Diplom.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "subscriptions")
public class Subscription {

    @Id
    private String id;

    private String userId;

    private String planCode; // BASIC, PRO
    private String provider; // PAYPAL

    private String paypalPlanId;
    private String paypalSubscriptionId;

    private SubscriptionStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime nextBillingTime;
    private LocalDateTime endedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}