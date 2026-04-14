package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateProducer {

    private final RabbitTemplate rabbitTemplate;

    public void requestCertificate(String userId, String courseId) {
        CertificateMessage message = new CertificateMessage(userId, courseId);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.CERTIFICATE_EXCHANGE,
                RabbitMQConfig.CERTIFICATE_ROUTING_KEY,
                message
        );
        log.info("Queued certificate generation for userId={}, courseId={}", userId, courseId);
    }
}