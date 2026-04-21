package com.diploma.Diplom.controller;

import com.diploma.Diplom.model.ActivityFeed;
import com.diploma.Diplom.service.ActivityFeedService;
import com.diploma.Diplom.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
public class ActivityFeedController {

    private final ActivityFeedService activityFeedService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<ActivityFeed> getMyActivity() {
        return activityFeedService.getRecentActivity(securityUtils.getCurrentUserId());
    }
}
