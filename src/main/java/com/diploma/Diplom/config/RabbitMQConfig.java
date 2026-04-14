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
}