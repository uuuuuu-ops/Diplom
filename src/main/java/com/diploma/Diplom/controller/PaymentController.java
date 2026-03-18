package com.diploma.Diplom.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.diploma.Diplom.dto.CreateSubscriptionRequest;
import com.diploma.Diplom.dto.MockPaymentRequest;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.service.PaymentService;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('STUDENT')")
    public Payment createPayment(@RequestBody CreateSubscriptionRequest request) {
        return paymentService.createPaymentForSubscription(request);
    }

    @PostMapping("/mock-pay")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public Payment mockPay(@RequestBody MockPaymentRequest request) {
        return paymentService.mockPay(request.getPaymentId());
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('STUDENT','ADMIN')")
    public List<Payment> getUserPayments(@PathVariable String userId) {
        return paymentService.getPaymentsByUser(userId);
    }
}