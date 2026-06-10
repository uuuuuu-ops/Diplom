package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableRabbit
public class EmailConsumer {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;

    @Value("${brevo.api-key}")
    private String brevoApiKey;

    @Value("${brevo.sender-email}")
    private String senderEmail;

    @Value("${brevo.sender-name:CouTeach}")
    private String senderName;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmail(EmailMessage message) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", java.util.List.of(Map.of("email", message.getTo())),
                    "subject", message.getSubject(),
                    "textContent", message.getBody()
            );

            restTemplate.postForEntity(BREVO_API_URL, new HttpEntity<>(payload, headers), String.class);
            log.info("Email sent to: {}", message.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", message.getTo(), e.getMessage());
            throw e;
        }
    }
}