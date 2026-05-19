package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.model.PaymentStatus;
import com.diploma.Diplom.model.PaymentType;
import com.diploma.Diplom.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String PROVIDER_PAYPAL = "PAYPAL";

    private final PaymentRepository paymentRepository;

    public Payment createCoursePayment(String userId,
                                       String courseId,
                                       String paypalOrderId,
                                       String approvalUrl,
                                       BigDecimal amount,
                                       String currency) {
        Payment payment = new Payment();
        payment.setUserId(userId);
        payment.setCourseId(courseId);
        payment.setType(PaymentType.COURSE_PURCHASE);
        payment.setStatus(PaymentStatus.CREATED);
        payment.setAmount(amount);
        payment.setCurrency(currency);
        payment.setProvider(PROVIDER_PAYPAL);
        payment.setPaypalOrderId(paypalOrderId);
        payment.setApprovalUrl(approvalUrl);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());

        return paymentRepository.save(payment);
    }

    public Payment markAsCaptured(String orderId, String captureId) {
        Payment payment = getByOrderId(orderId);
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setPaypalCaptureId(captureId);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment markAsFailed(String orderId) {
        Payment payment = getByOrderId(orderId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setUpdatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    public Payment getByOrderId(String orderId) {
        return paymentRepository.findByPaypalOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + orderId));
    }

    public List<Payment> getPaymentsByUser(String userId) {
        return paymentRepository.findByUserId(userId);
    }
}
 