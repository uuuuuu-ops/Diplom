package com.diploma.Diplom.service;

import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.model.ActivityType;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityFeedService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final ActivityFeedRepository repository;

    /**
     * Синхронное добавление активности (используется там, где нет Rabbit).
     * Инвалидирует кеш ленты этого пользователя.
     */
    @CacheEvict(value = "activityFeed", key = "#userId")
    public void addActivity(String userId,
                            ActivityType type,
                            String referenceId,
                            String message) {
        ActivityFeed activity = ActivityFeed.builder()
                .userId(userId)
                .type(type.name())
                .referenceId(referenceId)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(activity);
    }

    /**
     * Кешируем последние 20 записей на 2 минуты.
     * Используется контроллером ActivityFeedController.
     */
    @Cacheable(value = "activityFeed", key = "#userId")
    public List<ActivityFeed> getRecentActivity(String userId) {
        return repository.findByUserId(
                userId,
                PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
