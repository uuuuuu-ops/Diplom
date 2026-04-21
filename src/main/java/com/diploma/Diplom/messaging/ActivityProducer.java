package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendActivity(String userId, String type, String referenceId, String message) {
        ActivityMessage activityMessage = new ActivityMessage(userId, type, referenceId, message);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ACTIVITY_EXCHANGE,
                RabbitMQConfig.ACTIVITY_ROUTING_KEY,
                activityMessage
        );
        log.info("Queued activity userId={} type={}", userId, type);
    }
}