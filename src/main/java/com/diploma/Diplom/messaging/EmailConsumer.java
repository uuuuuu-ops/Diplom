package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@EnableRabbit
public class EmailConsumer {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmail(EmailMessage message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(message.getTo());
            mail.setSubject(message.getSubject());
            mail.setText(message.getBody());
            mailSender.send(mail);
            log.info("Email sent to: {}", message.getTo());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", message.getTo(), e.getMessage());
            throw e;
        }
    }
}