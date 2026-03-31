package com.diploma.Diplom.controller;

import com.diploma.Diplom.dto.CapturePaypalOrderRequest;
import com.diploma.Diplom.dto.CreatePaypalOrderResponse;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.service.PaypalService;
import com.diploma.Diplom.service.PaymentService;
import com.diploma.Diplom.service.EnrollmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments/paypal")
public class PaymentController {

    private final PaypalService paypalService;
    private final PaymentService paymentService;
    private final EnrollmentService enrollmentService;

    public PaymentController(PaypalService paypalService,
                             PaymentService paymentService,
                             EnrollmentService enrollmentService) {
        this.paypalService = paypalService;
        this.paymentService = paymentService;
        this.enrollmentService = enrollmentService;
    }

    // Создать PayPal order для покупки курса
    @PostMapping("/orders/course/{courseId}")
    public CreatePaypalOrderResponse createCourseOrder(@PathVariable String courseId) {
        return paypalService.createCourseOrder(courseId);
    }

    // Capture заказа после approve в PayPal
    @PostMapping("/orders/capture")
    public Payment captureOrder(@RequestBody CapturePaypalOrderRequest request) {
        return paypalService.captureOrder(request.getOrderId());
    }

    // Посмотреть мои платежи
    @GetMapping("/my")
    public List<Payment> getMyPayments() {
        String userId = enrollmentService.getCurrentUserId();
        return paymentService.getPaymentsByUser(userId);
    }
}