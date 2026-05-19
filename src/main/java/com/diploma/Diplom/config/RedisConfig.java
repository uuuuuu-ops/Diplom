package com.diploma.Diplom.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );
        return mapper;
    }

    @Bean
public RedisCacheManager cacheManager(RedisConnectionFactory factory,
                                      ObjectMapper redisObjectMapper) {

    RedisSerializer<Object> jsonSerializer =
            RedisSerializer.json();

    RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                    RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
            .disableCachingNullValues();

    return RedisCacheManager.builder(factory)
            .cacheDefaults(defaults)
            .withCacheConfiguration("course",
                    defaults.entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("courses",
                    defaults.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("courseRating",
                    defaults.entryTtl(Duration.ofMinutes(10)))
            .withCacheConfiguration("access",
                    defaults.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("activityFeed",
                    defaults.entryTtl(Duration.ofMinutes(2)))
            .withCacheConfiguration("progress",
                    defaults.entryTtl(Duration.ofMinutes(5)))
            .withCacheConfiguration("subscription",
                    defaults.entryTtl(Duration.ofMinutes(3)))
            .build();
}
}
