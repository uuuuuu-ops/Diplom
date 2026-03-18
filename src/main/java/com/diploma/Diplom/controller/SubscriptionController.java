package com.diploma.Diplom.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.repository.SubscriptionRepository;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionController(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public List<Subscription> getUserSubscriptions(@PathVariable String userId) {
        return subscriptionRepository.findByUserId(userId);
    }
}