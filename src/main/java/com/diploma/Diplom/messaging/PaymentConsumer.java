package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.repository.UserRepository;
import com.diploma.Diplom.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * После payment.captured — активирует enrollment и шлёт invoice email.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConsumer {

    private final EnrollmentService enrollmentService;
    private final EmailProducer emailProducer;
    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.PAYMENT_QUEUE)
    public void handlePaymentCaptured(PaymentCapturedMessage message) {
        try {
            enrollmentService.activatePurchasedEnrollment(
                    message.getUserId(),
                    message.getCourseId(),
                    message.getPaymentId()
            );

            // Шлём invoice email на настоящий адрес пользователя
            userRepository.findById(message.getUserId()).ifPresent(user -> {
                String body = String.format(
                        "Payment confirmed: %.2f %s for course %s\nPayment ID: %s",
                        message.getAmount(), message.getCurrency(),
                        message.getCourseId(), message.getPaymentId());
                emailProducer.sendEmail(user.getEmail(), "Payment Confirmed", body);
            });

            log.info("Processed payment.captured userId={} courseId={}",
                    message.getUserId(), message.getCourseId());
        } catch (Exception e) {
            log.error("Failed to process payment.captured: {}", e.getMessage());
            throw e;
        }
    }
}
