package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.CreateSubscriptionRequest;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.model.PaymentStatus;
import com.diploma.Diplom.model.Subscription;
import com.diploma.Diplom.model.SubscriptionStatus;
import com.diploma.Diplom.repository.PaymentRepository;
import com.diploma.Diplom.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    public PaymentService(PaymentRepository paymentRepository,
                          SubscriptionRepository subscriptionRepository,
                          SubscriptionService subscriptionService) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionService = subscriptionService;
    }

    public Payment createPaymentForSubscription(CreateSubscriptionRequest request) {
        Subscription subscription = subscriptionService.createPendingSubscription(request);

        Payment payment = new Payment();
        payment.setUserId(request.getUserId());
        payment.setSubscriptionId(subscription.getId());
        payment.setAmount(calculateAmount(request.getType()));
        payment.setCurrency("KZT");
        payment.setProvider("MOCK");
        payment.setStatus(PaymentStatus.PENDING);
        payment.setExternalPaymentId("MOCK-" + System.currentTimeMillis());
        payment.setCreatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment mockPay(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        Subscription subscription = subscriptionRepository.findById(payment.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setActive(true);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setEndDate(calculateEndDate(subscription.getType()));
        subscriptionRepository.save(subscription);

        return savedPayment;
    }

    public List<Payment> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId);
    }

    private double calculateAmount(String type) {
        return switch (type.toUpperCase()) {
            case "PRO" -> 5000;
            case "CORPORATE" -> 25000;
            case "FREE" -> 0;
            default -> throw new RuntimeException("Unknown subscription type");
        };
    }

    private LocalDateTime calculateEndDate(String type) {
        return switch (type.toUpperCase()) {
            case "PRO" -> LocalDateTime.now().plusMonths(1);
            case "CORPORATE" -> LocalDateTime.now().plusMonths(1);
            case "FREE" -> LocalDateTime.now().plusDays(7);
            default -> throw new RuntimeException("Unknown subscription type");
        };
    }
}