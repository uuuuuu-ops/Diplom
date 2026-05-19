package com.diploma.Diplom.service;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.messaging.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Messaging Producers Tests")
class MessagingProducersTest {

    @Mock RabbitTemplate rabbitTemplate;


    @InjectMocks EnrollmentProducer enrollmentProducer;

    @Test
    @DisplayName("EnrollmentProducer: шлёт сообщение на правильный exchange и routing key")
    void enrollmentProducer_sendsToCorrectExchange() {
        enrollmentProducer.sendEnrollmentEvent("user-1", "course-1", "FREE");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ENROLLMENT_EXCHANGE),
                eq(RabbitMQConfig.ENROLLMENT_ROUTING_KEY),
                any(EnrollmentMessage.class)
        );
    }

    @Test
    @DisplayName("EnrollmentProducer: сообщение содержит правильные данные")
    void enrollmentProducer_messageContainsCorrectData() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        enrollmentProducer.sendEnrollmentEvent("user-42", "course-99", "PURCHASE");

        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        EnrollmentMessage msg = (EnrollmentMessage) captor.getValue();
        assertThat(msg.getUserId()).isEqualTo("user-42");
        assertThat(msg.getCourseId()).isEqualTo("course-99");
        assertThat(msg.getAccessType()).isEqualTo("PURCHASE");
    }
}

@ExtendWith(MockitoExtension.class)
class SubscriptionProducerTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks SubscriptionProducer subscriptionProducer;

    @Test
    @DisplayName("SubscriptionProducer: шлёт сообщение на правильный exchange и routing key")
    void subscriptionProducer_sendsToCorrectExchange() {
        subscriptionProducer.sendSubscriptionEvent("user-1", "user@test.com", "ACTIVATED", "BASIC");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.SUBSCRIPTION_EXCHANGE),
                eq(RabbitMQConfig.SUBSCRIPTION_ROUTING_KEY),
                any(SubscriptionMessage.class)
        );
    }

    @Test
    @DisplayName("SubscriptionProducer: сообщение содержит все поля")
    void subscriptionProducer_messageContainsAllFields() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        subscriptionProducer.sendSubscriptionEvent("user-1", "u@test.com", "CANCELLED", "PRO");

        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        SubscriptionMessage msg = (SubscriptionMessage) captor.getValue();
        assertThat(msg.getUserId()).isEqualTo("user-1");
        assertThat(msg.getEmail()).isEqualTo("u@test.com");
        assertThat(msg.getEventType()).isEqualTo("CANCELLED");
        assertThat(msg.getPlanCode()).isEqualTo("PRO");
    }
}

@ExtendWith(MockitoExtension.class)
class ActivityProducerTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks ActivityProducer activityProducer;

    @Test
    @DisplayName("ActivityProducer: шлёт сообщение на правильный exchange")
    void activityProducer_sendsToCorrectExchange() {
        activityProducer.sendActivity("user-1", "ENROLLMENT", "course-1", "Enrolled");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.ACTIVITY_EXCHANGE),
                eq(RabbitMQConfig.ACTIVITY_ROUTING_KEY),
                any(ActivityMessage.class)
        );
    }

    @Test
    @DisplayName("ActivityProducer: сообщение содержит все поля")
    void activityProducer_messageContainsAllFields() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        activityProducer.sendActivity("user-5", "LIKE", "course-5", "Liked a course");

        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        ActivityMessage msg = (ActivityMessage) captor.getValue();
        assertThat(msg.getUserId()).isEqualTo("user-5");
        assertThat(msg.getType()).isEqualTo("LIKE");
        assertThat(msg.getReferenceId()).isEqualTo("course-5");
        assertThat(msg.getMessage()).isEqualTo("Liked a course");
    }
}

@ExtendWith(MockitoExtension.class)
class PaymentProducerTest {

    @Mock RabbitTemplate rabbitTemplate;
    @InjectMocks PaymentProducer paymentProducer;

    @Test
    @DisplayName("PaymentProducer: шлёт сообщение на правильный exchange")
    void paymentProducer_sendsToCorrectExchange() {
        paymentProducer.sendPaymentCaptured("user-1", "course-1", "pay-1",
                new BigDecimal("29.99"), "USD");

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.PAYMENT_EXCHANGE),
                eq(RabbitMQConfig.PAYMENT_ROUTING_KEY),
                any(PaymentCapturedMessage.class)
        );
    }

    @Test
    @DisplayName("PaymentProducer: сообщение содержит все поля")
    void paymentProducer_messageContainsAllFields() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);

        paymentProducer.sendPaymentCaptured("user-2", "course-2", "pay-99",
                new BigDecimal("49.99"), "EUR");

        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        PaymentCapturedMessage msg = (PaymentCapturedMessage) captor.getValue();
        assertThat(msg.getUserId()).isEqualTo("user-2");
        assertThat(msg.getCourseId()).isEqualTo("course-2");
        assertThat(msg.getPaymentId()).isEqualTo("pay-99");
        assertThat(msg.getAmount()).isEqualByComparingTo("49.99");
        assertThat(msg.getCurrency()).isEqualTo("EUR");
    }
}