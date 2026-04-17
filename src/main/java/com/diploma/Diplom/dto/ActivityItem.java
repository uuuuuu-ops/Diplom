package com.diploma.Diplom.dto;

import java.time.LocalDateTime;

public record ActivityItem(
        String type,
        String message,
        LocalDateTime createdAt
) {}