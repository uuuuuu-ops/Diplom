package com.diploma.Diplom.dto;

import java.util.List;

public record ProfileResponse(
        String id,
        String name,
        Integer age,
        String profileImageUrl,
        List<ActivityItem> activity
) {}