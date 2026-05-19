package com.diploma.Diplom.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Queue names
    public static final String EMAIL_QUEUE        = "email.queue";
    public static final String CERTIFICATE_QUEUE  = "certificate.queue";

    // Exchange names
    public static final String EMAIL_EXCHANGE       = "email.exchange";
    public static final String CERTIFICATE_EXCHANGE = "certificate.exchange";

    // Routing keys
    public static final String EMAIL_ROUTING_KEY       = "email.send";
    public static final String CERTIFICATE_ROUTING_KEY = "certificate.generate";

    // Dead-letter queues
    public static final String EMAIL_DLQ       = "email.dlq";
    public static final String CERTIFICATE_DLQ = "certificate.dlq";

    public static final String NOTIFICATION_QUEUE       = "notification.queue";
    public static final String NOTIFICATION_EXCHANGE    = "notification.exchange";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.comment";
    public static final String NOTIFICATION_DLQ         = "notification.dlq";

    // Payment
    public static final String PAYMENT_QUEUE       = "payment.queue";
    public static final String PAYMENT_EXCHANGE    = "payment.exchange";
    public static final String PAYMENT_ROUTING_KEY = "payment.captured";
    public static final String PAYMENT_DLQ         = "payment.dlq";

    // Activity
    public static final String ACTIVITY_QUEUE       = "activity.queue";
    public static final String ACTIVITY_EXCHANGE    = "activity.exchange";
    public static final String ACTIVITY_ROUTING_KEY = "activity.send";
    public static final String ACTIVITY_DLQ         = "activity.dlq";

    // Enrollment
    public static final String ENROLLMENT_QUEUE       = "enrollment.queue";
    public static final String ENROLLMENT_EXCHANGE    = "enrollment.exchange";
    public static final String ENROLLMENT_ROUTING_KEY = "enrollment.created";
    public static final String ENROLLMENT_DLQ         = "enrollment.dlq";

    // Subscription
    public static final String SUBSCRIPTION_QUEUE       = "subscription.queue";
    public static final String SUBSCRIPTION_EXCHANGE    = "subscription.exchange";
    public static final String SUBSCRIPTION_ROUTING_KEY = "subscription.event";
    public static final String SUBSCRIPTION_DLQ         = "subscription.dlq";

    // --- Email ---
    @Bean
    public Queue emailQueue() {
        return QueueBuilder.durable(EMAIL_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", EMAIL_DLQ)
                .build();
    }

    @Bean
    public Queue emailDlq() {
        return QueueBuilder.durable(EMAIL_DLQ).build();
    }

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(EMAIL_EXCHANGE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange emailExchange) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(EMAIL_ROUTING_KEY);
    }

    // --- Certificate ---
    @Bean
    public Queue certificateQueue() {
        return QueueBuilder.durable(CERTIFICATE_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", CERTIFICATE_DLQ)
                .build();
    }

    @Bean
    public Queue certificateDlq() {
        return QueueBuilder.durable(CERTIFICATE_DLQ).build();
    }

    @Bean
    public DirectExchange certificateExchange() {
        return new DirectExchange(CERTIFICATE_EXCHANGE);
    }

    @Bean
    public Binding certificateBinding(Queue certificateQueue, DirectExchange certificateExchange) {
        return BindingBuilder.bind(certificateQueue).to(certificateExchange).with(CERTIFICATE_ROUTING_KEY);
    }

    // --- Converter & Template ---
    @Bean
    public JacksonJsonMessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }


    @Bean
    public Binding emailDlqBinding(Queue emailDlq) {
        return BindingBuilder
            .bind(emailDlq)
            .to(new DirectExchange(""))
            .with(EMAIL_DLQ);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .build();
    }

    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // --- Payment ---
    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", PAYMENT_DLQ)
                .build();
    }

    @Bean
    public Queue paymentDlq() {
        return QueueBuilder.durable(PAYMENT_DLQ).build();
    }

    @Bean
    public DirectExchange paymentExchange() {
        return new DirectExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentBinding(Queue paymentQueue, DirectExchange paymentExchange) {
        return BindingBuilder.bind(paymentQueue).to(paymentExchange).with(PAYMENT_ROUTING_KEY);
    }

    // --- Activity ---
    @Bean
    public Queue activityQueue() {
        return QueueBuilder.durable(ACTIVITY_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ACTIVITY_DLQ)
                .build();
    }

    @Bean
    public Queue activityDlq() {
        return QueueBuilder.durable(ACTIVITY_DLQ).build();
    }

    @Bean
    public DirectExchange activityExchange() {
        return new DirectExchange(ACTIVITY_EXCHANGE);
    }

    @Bean
    public Binding activityBinding(Queue activityQueue, DirectExchange activityExchange) {
        return BindingBuilder.bind(activityQueue).to(activityExchange).with(ACTIVITY_ROUTING_KEY);
    }

    // --- Enrollment ---
    @Bean
    public Queue enrollmentQueue() {
        return QueueBuilder.durable(ENROLLMENT_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", ENROLLMENT_DLQ)
                .build();
    }

    @Bean
    public Queue enrollmentDlq() {
        return QueueBuilder.durable(ENROLLMENT_DLQ).build();
    }

    @Bean
    public DirectExchange enrollmentExchange() {
        return new DirectExchange(ENROLLMENT_EXCHANGE);
    }

    @Bean
    public Binding enrollmentBinding(Queue enrollmentQueue, DirectExchange enrollmentExchange) {
        return BindingBuilder.bind(enrollmentQueue).to(enrollmentExchange).with(ENROLLMENT_ROUTING_KEY);
    }

    // --- Subscription ---
    @Bean
    public Queue subscriptionQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_QUEUE)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", SUBSCRIPTION_DLQ)
                .build();
    }

    @Bean
    public Queue subscriptionDlq() {
        return QueueBuilder.durable(SUBSCRIPTION_DLQ).build();
    }

    @Bean
    public DirectExchange subscriptionExchange() {
        return new DirectExchange(SUBSCRIPTION_EXCHANGE);
    }

    @Bean
    public Binding subscriptionBinding(Queue subscriptionQueue, DirectExchange subscriptionExchange) {
        return BindingBuilder.bind(subscriptionQueue).to(subscriptionExchange).with(SUBSCRIPTION_ROUTING_KEY);
    }

}