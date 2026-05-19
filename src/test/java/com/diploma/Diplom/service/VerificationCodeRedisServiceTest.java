package com.diploma.Diplom.service;

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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VerificationCodeRedisService Tests")
class VerificationCodeRedisServiceTest {

    @Mock RedisTemplate<String, String> redisTemplate;
    @Mock ValueOperations<String, String> valueOps;

    @InjectMocks
    VerificationCodeRedisService service;

    // FIX: убран глобальный @BeforeEach с when(redisTemplate.opsForValue()).
    // Метод delete() не вызывает opsForValue() — общий стаб провоцировал
    // UnnecessaryStubbingException. Теперь каждый тест настраивает свой мок.

    // ─────────────────────── save ────────────────────────────────────────

    @Test
    @DisplayName("save: сохраняет код с префиксом и TTL 10 минут")
    void save_storesWithPrefixAndTtl() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        service.save("user@test.com", "123456");

        verify(valueOps).set("verification:user@test.com", "123456", Duration.ofMinutes(10));
    }

    // ─────────────────────── findCode ────────────────────────────────────

    @Test
    @DisplayName("findCode: код существует — возвращает Optional с кодом")
    void findCode_exists_returnsOptional() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn("123456");

        Optional<String> result = service.findCode("user@test.com");

        assertThat(result).isPresent().hasValue("123456");
    }

    @Test
    @DisplayName("findCode: код не существует — возвращает пустой Optional")
    void findCode_notExists_returnsEmpty() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn(null);

        Optional<String> result = service.findCode("user@test.com");

        assertThat(result).isEmpty();
    }

    // ─────────────────────── verify ──────────────────────────────────────

    @Test
    @DisplayName("verify: правильный код — возвращает true")
    void verify_correctCode_returnsTrue() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn("123456");

        boolean result = service.verify("user@test.com", "123456");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("verify: неправильный код — возвращает false")
    void verify_wrongCode_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn("123456");

        boolean result = service.verify("user@test.com", "999999");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verify: код не существует в Redis (TTL истёк) — возвращает false")
    void verify_expiredCode_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn(null);

        boolean result = service.verify("user@test.com", "123456");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verify: код null в запросе — возвращает false")
    void verify_nullCode_returnsFalse() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("verification:user@test.com")).thenReturn("123456");

        boolean result = service.verify("user@test.com", null);

        assertThat(result).isFalse();
    }

    // ─────────────────────── delete ──────────────────────────────────────

    @Test
    @DisplayName("delete: удаляет ключ с правильным префиксом")
    void delete_removesKey() {
        // FIX: delete() вызывает redisTemplate.delete() напрямую, без opsForValue()
        // Поэтому никакой настройки opsForValue не нужно

        service.delete("user@test.com");

        verify(redisTemplate).delete("verification:user@test.com");
    }

    @Test
    @DisplayName("delete: разные email — разные ключи")
    void delete_differentEmails_differentKeys() {
        service.delete("alice@test.com");
        service.delete("bob@test.com");

        verify(redisTemplate).delete("verification:alice@test.com");
        verify(redisTemplate).delete("verification:bob@test.com");
    }
}