package com.diploma.Diplom.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class VerificationCodeRedisService {

    private static final String PREFIX = "verification:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final RedisTemplate<String, String> redisTemplate;

    public VerificationCodeRedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void save(String email, String code) {
        redisTemplate.opsForValue().set(PREFIX + email, code, TTL);
    }

    public Optional<String> findCode(String email) {
        String code = redisTemplate.opsForValue().get(PREFIX + email);
        return Optional.ofNullable(code);
    }

    public boolean verify(String email, String code) {
        String stored = redisTemplate.opsForValue().get(PREFIX + email);
        return code != null && code.equals(stored);
    }

    public void delete(String email) {
        redisTemplate.delete(PREFIX + email);
    }
}