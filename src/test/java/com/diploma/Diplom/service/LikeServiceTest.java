package com.diploma.Diplom.service;

import com.diploma.Diplom.dto.LikeResponse;
import com.diploma.Diplom.model.Like;
import com.diploma.Diplom.repository.LikeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LikeService Tests")
class LikeServiceTest {

    @Mock LikeRepository likeRepository;
    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks LikeService likeService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    @DisplayName("toggle: не лайкнуто — создаёт лайк, возвращает liked=true")
    void toggle_notLiked_createsLike() {
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(valueOps.increment("likes:course:course-1")).thenReturn(1L);

        LikeResponse result = likeService.toggle("course-1", "user-1");

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTotalLikes()).isEqualTo(1L);
        verify(likeRepository).save(any(Like.class));
    }

    @Test
    @DisplayName("toggle: уже лайкнуто — удаляет лайк, возвращает liked=false")
    void toggle_alreadyLiked_removesLike() {
        Like existing = new Like();
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(existing));
        when(valueOps.decrement("likes:course:course-1")).thenReturn(2L);

        LikeResponse result = likeService.toggle("course-1", "user-1");

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getTotalLikes()).isEqualTo(2L);
        verify(likeRepository).deleteByUserIdAndCourseId("user-1", "course-1");
        verify(likeRepository, never()).save(any());
    }

    @Test
    @DisplayName("toggle: счётчик в Redis null — возвращает count из БД")
    void toggle_redisNullCount_fallsBackToDb() {
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(valueOps.increment("likes:course:course-1")).thenReturn(null);
        when(likeRepository.countByCourseId("course-1")).thenReturn(5L);

        LikeResponse result = likeService.toggle("course-1", "user-1");

        assertThat(result.getTotalLikes()).isEqualTo(5L);
    }


    @Test
    @DisplayName("getStatus: пользователь лайкнул — liked=true с кешированным счётчиком")
    void getStatus_liked_returnsTrue() {
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.of(new Like()));
        when(valueOps.get("likes:course:course-1")).thenReturn("10");

        LikeResponse result = likeService.getStatus("course-1", "user-1");

        assertThat(result.isLiked()).isTrue();
        assertThat(result.getTotalLikes()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getStatus: пользователь не лайкал — liked=false")
    void getStatus_notLiked_returnsFalse() {
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(valueOps.get("likes:course:course-1")).thenReturn("3");

        LikeResponse result = likeService.getStatus("course-1", "user-1");

        assertThat(result.isLiked()).isFalse();
        assertThat(result.getTotalLikes()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getStatus: кеш Redis пуст — берёт count из БД и кеширует")
    void getStatus_cacheEmpty_populatesFromDb() {
        when(likeRepository.findByUserIdAndCourseId("user-1", "course-1"))
                .thenReturn(Optional.empty());
        when(valueOps.get("likes:course:course-1")).thenReturn(null);
        when(likeRepository.countByCourseId("course-1")).thenReturn(7L);

        LikeResponse result = likeService.getStatus("course-1", "user-1");

        assertThat(result.getTotalLikes()).isEqualTo(7L);
        verify(valueOps).set(eq("likes:course:course-1"), eq("7"), any(Duration.class));
    }
}
