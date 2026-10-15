package com.ledgerwallet.userservice.infrastructure.cache;

import com.ledgerwallet.userservice.application.dtos.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${spring.cache.redis.time-to-live:5m}") Duration cacheTtl
    ) {
        var jsonSerializer = new JacksonJsonRedisSerializer<>(objectMapper, UserResponse.class);
        var cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(cacheTtl)
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }
}
