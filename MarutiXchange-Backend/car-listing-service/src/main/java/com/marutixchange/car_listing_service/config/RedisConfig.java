package com.marutixchange.car_listing_service.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for car-listing-service.
 *
 * Only active in production profile (--spring.profiles.active=prod).
 * In local/dev, Redis is not required — service starts without it.
 */
@Configuration
@EnableCaching
@Profile("prod")   // Redis only in production — dev/local starts without Redis
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("car-listings",     defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigs.put("car-search",       defaultConfig.entryTtl(Duration.ofMinutes(2)));
        cacheConfigs.put("car-pricing",      defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigs.put("seller-analytics", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        cacheConfigs.put("featured-cars",    defaultConfig.entryTtl(Duration.ofMinutes(15)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}