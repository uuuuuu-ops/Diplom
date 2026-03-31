package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.ConfirmPaypalSubscriptionRequest;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.service.PaypalSubscriptionService;
import com.diploma.Diplom.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subscriptions/paypal")
public class SubscriptionController {

    private final PaypalSubscriptionService paypalSubscriptionService;
    private final SubscriptionService subscriptionService;

    public SubscriptionController(PaypalSubscriptionService paypalSubscriptionService,
                                  SubscriptionService subscriptionService) {
        this.paypalSubscriptionService = paypalSubscriptionService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/plan")
    public Map<String, String> getPlan() {
        return Map.of("planId", paypalSubscriptionService.getPlanId());
    }

    @PostMapping("/confirm")
    public Subscription confirm(@RequestBody ConfirmPaypalSubscriptionRequest request) {
        return paypalSubscriptionService.confirmSubscription(request.getSubscriptionId());
    }

    @PostMapping("/save-pending")
    public Subscription savePending(@RequestBody ConfirmPaypalSubscriptionRequest request) {
        return paypalSubscriptionService.savePendingSubscription(
                request.getSubscriptionId(),
                "PRO"
        );
    }

    @GetMapping("/my")
    public List<Subscription> mySubscriptions() {
        return subscriptionService.getMySubscriptions();
    }
}