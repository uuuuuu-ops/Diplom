package com.diploma.Diplom.messaging;

import com.diploma.Diplom.config.RabbitMQConfig;
import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationConsumer {

    private final ActivityFeedRepository activityFeedRepository;
    private final CacheManager cacheManager;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleCommentNotification(CommentNotificationMessage message) {
        try {
            String activityMessage = String.format(
                    "%s commented on your %s: \"%s\"",
                    message.getAuthorName(),
                    message.getTargetType().name().toLowerCase(),
                    message.getCommentPreview()
            );

            ActivityFeed activity = ActivityFeed.builder()
                    .userId(message.getTargetId())
                    .type("COMMENT_RECEIVED")
                    .referenceId(message.getAuthorId())
                    .message(activityMessage)
                    .createdAt(LocalDateTime.now())
                    .build();

            activityFeedRepository.save(activity);

            // Инвалидируем кеш ленты владельца контента
            var cache = cacheManager.getCache("activityFeed");
            if (cache != null) {
                cache.evict(message.getTargetId());
            }

            log.info("Saved comment notification for targetId={}", message.getTargetId());
        } catch (Exception e) {
            log.error("Failed to process comment notification: {}", e.getMessage());
            throw e;
        }
    }
}
