package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendVerificationEmail(String to, String code) {
        sendEmail(to, "Account Verification", "Your verification code: " + code);
    }

    public void sendEmail(String to, String subject, String body) {
        EmailMessage message = new EmailMessage(to, subject, body);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );
        log.info("Queued email to={} subject={}", to, subject);
    }
}
