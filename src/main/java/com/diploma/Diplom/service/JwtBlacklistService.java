package com.diploma.Diplom.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;


@Service
public class JwtBlacklistService {

    private static final String PREFIX = "jwt:blacklist:";

    private final RedisTemplate<String, String> redisTemplate;

    public JwtBlacklistService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void blacklist(String jti, Duration ttl) {
        if (ttl.isPositive()) {
            redisTemplate.opsForValue().set(PREFIX + jti, "1", ttl);
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + jti));
    }
}
