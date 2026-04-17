package com.diploma.Diplom.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.model.ActivityType;
import com.diploma.Diplom.repository.ActivityFeedRepository;

@Service
public class ActivityFeedService {

    private final ActivityFeedRepository repository;

    public ActivityFeedService(ActivityFeedRepository repository) {
        this.repository = repository;
    }

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
}