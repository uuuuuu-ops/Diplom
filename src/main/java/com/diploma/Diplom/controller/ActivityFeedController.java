package com.diploma.Diplom.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.repository.ActivityFeedRepository;
import com.diploma.Diplom.util.SecurityUtils;

@RestController
@RequestMapping("/activity")
public class ActivityFeedController {

    private final ActivityFeedRepository repository;
    private final SecurityUtils securityUtils;

    public ActivityFeedController(ActivityFeedRepository repository,
                                  SecurityUtils securityUtils) {
        this.repository = repository;
        this.securityUtils = securityUtils;
    }

    @GetMapping
    public List<ActivityFeed> getMyActivity() {
        String userId = securityUtils.getCurrentUserId();
        return repository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}