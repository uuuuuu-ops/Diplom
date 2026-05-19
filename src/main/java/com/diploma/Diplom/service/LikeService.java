package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.LikeResponse;
import com.diploma.Diplom.model.Like;
import com.diploma.Diplom.repository.LikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeService {

    private static final String LIKES_KEY = "likes:course:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final LikeRepository likeRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public LikeResponse toggle(String courseId, String userId) {
        Optional<Like> existing = likeRepository.findByUserIdAndCourseId(userId, courseId);

        if (existing.isPresent()) {
            likeRepository.deleteByUserIdAndCourseId(userId, courseId);
            long count = decrementCache(courseId);
            log.debug("User {} unliked course {}", userId, courseId);
            return new LikeResponse(false, count);
        } else {
            Like like = new Like();
            like.setUserId(userId);
            like.setCourseId(courseId);
            likeRepository.save(like);
            long count = incrementCache(courseId);
            log.debug("User {} liked course {}", userId, courseId);
            return new LikeResponse(true, count);
        }
    }

    public LikeResponse getStatus(String courseId, String userId) {
        boolean liked = likeRepository.findByUserIdAndCourseId(userId, courseId).isPresent();
        long count = getCachedCount(courseId);
        return new LikeResponse(liked, count);
    }

    private long getCachedCount(String courseId) {
        String key = LIKES_KEY + courseId;
        String cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return Long.parseLong(cached);
        }
        long count = likeRepository.countByCourseId(courseId);
        redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_TTL);
        return count;
    }

    private long incrementCache(String courseId) {
        String key = LIKES_KEY + courseId;
        Long result = redisTemplate.opsForValue().increment(key);
        if (result == null) {
            return likeRepository.countByCourseId(courseId);
        }
        redisTemplate.expire(key, CACHE_TTL);
        return result;
    }

    private long decrementCache(String courseId) {
        String key = LIKES_KEY + courseId;
        Long result = redisTemplate.opsForValue().decrement(key);
        if (result == null || result < 0) {
            long count = likeRepository.countByCourseId(courseId);
            redisTemplate.opsForValue().set(key, String.valueOf(count), CACHE_TTL);
            return count;
        }
        redisTemplate.expire(key, CACHE_TTL);
        return result;
    }
}
