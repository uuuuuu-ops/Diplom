package com.diploma.Diplom.service;

import com.diploma.Diplom.exception.ResourceNotFoundException;
import com.diploma.Diplom.model.Payment;
import com.diploma.Diplom.model.PaymentStatus;
import com.diploma.Diplom.model.PaymentType;
import com.diploma.Diplom.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Tests")
class PaymentServiceTest {

    @Mock PaymentRepository paymentRepository;

    @InjectMocks PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = new Payment();
        payment.setId("pay-1");
        payment.setUserId("user-1");
        payment.setCourseId("course-1");
        payment.setPaypalOrderId("PAYPAL-ORDER-123");
        payment.setStatus(PaymentStatus.CREATED);
        payment.setAmount(new BigDecimal("29.99"));
        payment.setCurrency("USD");
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
    }

    // ──────────────────── createCoursePayment ─────────────────────────────

    @Test
    @DisplayName("createCoursePayment: создаёт платёж с правильными полями")
    void createCoursePayment_createsWithCorrectFields() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createCoursePayment(
                "user-1", "course-1", "PAYPAL-ORDER-123",
                "https://paypal.com/approve", new BigDecimal("29.99"), "USD");

        assertThat(result.getUserId()).isEqualTo("user-1");
        assertThat(result.getCourseId()).isEqualTo("course-1");
        assertThat(result.getPaypalOrderId()).isEqualTo("PAYPAL-ORDER-123");
        assertThat(result.getApprovalUrl()).isEqualTo("https://paypal.com/approve");
        assertThat(result.getAmount()).isEqualByComparingTo("29.99");
        assertThat(result.getCurrency()).isEqualTo("USD");
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(result.getType()).isEqualTo(PaymentType.COURSE_PURCHASE);
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    @DisplayName("createCoursePayment: провайдер всегда PAYPAL")
    void createCoursePayment_providerIsPaypal() {
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createCoursePayment(
                "user-1", "course-1", "ORDER-1",
                "https://paypal.com/approve", new BigDecimal("9.99"), "USD");

        assertThat(result.getProvider()).isEqualTo("PAYPAL");
    }

    // ──────────────────── markAsCaptured ──────────────────────────────────

    @Test
    @DisplayName("markAsCaptured: статус становится CAPTURED, captureId сохраняется")
    void markAsCaptured_updatesStatusAndCaptureId() {
        when(paymentRepository.findByPaypalOrderId("PAYPAL-ORDER-123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.markAsCaptured("PAYPAL-ORDER-123", "CAPTURE-456");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
        assertThat(result.getPaypalCaptureId()).isEqualTo("CAPTURE-456");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("markAsCaptured: ордер не найден — ResourceNotFoundException")
    void markAsCaptured_notFound_throws() {
        when(paymentRepository.findByPaypalOrderId("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markAsCaptured("MISSING", "CAPTURE-1"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("MISSING");
    }

    // ──────────────────── markAsFailed ────────────────────────────────────

    @Test
    @DisplayName("markAsFailed: статус становится FAILED")
    void markAsFailed_updatesStatusToFailed() {
        when(paymentRepository.findByPaypalOrderId("PAYPAL-ORDER-123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.markAsFailed("PAYPAL-ORDER-123");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(paymentRepository).save(payment);
    }

    @Test
    @DisplayName("markAsFailed: ордер не найден — ResourceNotFoundException")
    void markAsFailed_notFound_throws() {
        when(paymentRepository.findByPaypalOrderId("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.markAsFailed("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ──────────────────── getByOrderId ────────────────────────────────────

    @Test
    @DisplayName("getByOrderId: возвращает платёж по orderId")
    void getByOrderId_found_returnsPayment() {
        when(paymentRepository.findByPaypalOrderId("PAYPAL-ORDER-123"))
                .thenReturn(Optional.of(payment));

        Payment result = paymentService.getByOrderId("PAYPAL-ORDER-123");

        assertThat(result.getId()).isEqualTo("pay-1");
        assertThat(result.getPaypalOrderId()).isEqualTo("PAYPAL-ORDER-123");
    }

    @Test
    @DisplayName("getByOrderId: не найден — ResourceNotFoundException с orderId в сообщении")
    void getByOrderId_notFound_throwsWithMessage() {
        when(paymentRepository.findByPaypalOrderId("BAD-ID")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getByOrderId("BAD-ID"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("BAD-ID");
    }

    // ──────────────────── getPaymentsByUser ───────────────────────────────

    @Test
    @DisplayName("getPaymentsByUser: возвращает все платежи пользователя")
    void getPaymentsByUser_returnsList() {
        Payment payment2 = new Payment();
        payment2.setId("pay-2");
        payment2.setUserId("user-1");

        when(paymentRepository.findByUserId("user-1"))
                .thenReturn(List.of(payment, payment2));

        List<Payment> result = paymentService.getPaymentsByUser("user-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Payment::getUserId).containsOnly("user-1");
    }

    @Test
    @DisplayName("getPaymentsByUser: нет платежей — возвращает пустой список")
    void getPaymentsByUser_noPayments_returnsEmptyList() {
        when(paymentRepository.findByUserId("user-no-payments")).thenReturn(List.of());

        List<Payment> result = paymentService.getPaymentsByUser("user-no-payments");

        assertThat(result).isEmpty();
    }

    // ──────────────────── состояния платежа ───────────────────────────────

    @Test
    @DisplayName("Статус CREATED не изменяется без вызова mark-методов")
    void payment_initialStatus_isCreated() {
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Payment result = paymentService.createCoursePayment(
                "u", "c", "O", "http://url", BigDecimal.TEN, "USD");

        assertThat(result.getStatus()).isEqualTo(PaymentStatus.CREATED);
    }

    @Test
    @DisplayName("markAsCaptured → markAsFailed: второй вызов перезаписывает статус")
    void markAsCapturedThenFailed_statusOverridden() {
        // Первый вызов: CAPTURED
        when(paymentRepository.findByPaypalOrderId("ORDER-1"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        paymentService.markAsCaptured("ORDER-1", "CAPTURE-1");

        payment.setStatus(PaymentStatus.CAPTURED);
        paymentService.markAsFailed("ORDER-1");

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
}