package com.diploma.Diplom.service;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.messaging.EmailMessage;
import com.diploma.Diplom.messaging.EmailProducer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailProducer Tests")
class EmailProducerTest {

    @Mock RabbitTemplate rabbitTemplate;

    @InjectMocks
    EmailProducer emailProducer;

    @Test
    @DisplayName("sendVerificationEmail: отправляет сообщение на правильный exchange и routing key")
    void sendVerificationEmail_sendsToCorrectExchange() {
        emailProducer.sendVerificationEmail("user@test.com", "123456");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EMAIL_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                any(EmailMessage.class)
        );
    }

    @Test
    @DisplayName("sendVerificationEmail: сообщение содержит правильный email и код")
    void sendVerificationEmail_messageContainsEmailAndCode() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        emailProducer.sendVerificationEmail("user@test.com", "999888");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EMAIL_EXCHANGE),
                eq(RabbitMQConfig.EMAIL_ROUTING_KEY),
                captor.capture()
        );

        EmailMessage msg = (EmailMessage) captor.getValue();
        assertThat(msg.getTo()).isEqualTo("user@test.com");
        assertThat(msg.getBody()).contains("999888");
        assertThat(msg.getSubject()).isNotBlank();
    }

    @Test
    @DisplayName("sendVerificationEmail: вызывается ровно один раз для одного запроса")
    void sendVerificationEmail_calledExactlyOnce() {
        emailProducer.sendVerificationEmail("user@test.com", "111222");

        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(Object.class));
    }
}