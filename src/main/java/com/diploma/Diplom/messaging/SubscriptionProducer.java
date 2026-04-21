package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscriptionProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendSubscriptionEvent(String userId, String email, String eventType, String planCode) {
        SubscriptionMessage message = new SubscriptionMessage(userId, email, eventType, planCode);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SUBSCRIPTION_EXCHANGE,
                RabbitMQConfig.SUBSCRIPTION_ROUTING_KEY,
                message
        );
        log.info("Queued subscription event userId={} eventType={} planCode={}", userId, eventType, planCode);
    }
}