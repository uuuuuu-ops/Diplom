package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.model.CommentTargetType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void notifyNewComment(
            CommentTargetType targetType,
            String targetId,
            String authorId,
            String authorName,
            String commentText
    ) {

        CommentNotificationMessage message =
                new CommentNotificationMessage(
                        targetType,
                        targetId,
                        authorId,
                        authorName,
                        truncate(commentText)
                );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.NOTIFICATION_EXCHANGE,
                RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                message
        );

        log.info("Queued comment notification: {} {}", targetType, targetId);
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 100 ? text.substring(0, 100) + "..." : text;
    }
}