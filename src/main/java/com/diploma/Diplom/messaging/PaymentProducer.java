package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentCaptured(String userId, String courseId, String paymentId,
                                    BigDecimal amount, String currency) {
        PaymentCapturedMessage message = new PaymentCapturedMessage(userId, courseId, paymentId, amount, currency);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PAYMENT_EXCHANGE,
                RabbitMQConfig.PAYMENT_ROUTING_KEY,
                message
        );
        log.info("Queued payment.captured userId={} courseId={} paymentId={}", userId, courseId, paymentId);
    }
}