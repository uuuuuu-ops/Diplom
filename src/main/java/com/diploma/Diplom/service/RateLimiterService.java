package com.diploma.Diplom.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;


@Service
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    public RateLimiterService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAllowed(String action, String identifier, int maxRequests, Duration window) {
        String key = "ratelimit:" + action + ":" + identifier;
        long now = Instant.now().toEpochMilli();
        long windowStart = now - window.toMillis();

        redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);

        Long count = redisTemplate.opsForZSet().zCard(key);
        if (count != null && count >= maxRequests) {
            return false;
        }

        redisTemplate.opsForZSet().add(key, now + ":" + UUID.randomUUID(), now);
        redisTemplate.expire(key, window.plusSeconds(10));

        return true;
    }
}