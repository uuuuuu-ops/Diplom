package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEnrollmentEvent(String userId, String courseId, String accessType) {
        EnrollmentMessage message = new EnrollmentMessage(userId, courseId, accessType);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ENROLLMENT_EXCHANGE,
                RabbitMQConfig.ENROLLMENT_ROUTING_KEY,
                message
        );
        log.info("Queued enrollment event userId={} courseId={} accessType={}", userId, courseId, accessType);
    }
}