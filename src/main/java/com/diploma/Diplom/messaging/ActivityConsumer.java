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
public class ActivityConsumer {

    private final ActivityFeedRepository activityFeedRepository;
    private final CacheManager cacheManager;

    @RabbitListener(queues = RabbitMQConfig.ACTIVITY_QUEUE)
    public void handleActivity(ActivityMessage message) {
        try {
            ActivityFeed activity = ActivityFeed.builder()
                    .userId(message.getUserId())
                    .type(message.getType())
                    .referenceId(message.getReferenceId())
                    .message(message.getMessage())
                    .createdAt(LocalDateTime.now())
                    .build();

            activityFeedRepository.save(activity);

            // Инвалидируем кеш ленты этого пользователя
            var cache = cacheManager.getCache("activityFeed");
            if (cache != null) {
                cache.evict(message.getUserId());
            }

            log.debug("Saved activity userId={} type={}", message.getUserId(), message.getType());
        } catch (Exception e) {
            log.error("Failed to save activity: {}", e.getMessage());
            throw e;
        }
    }
}
