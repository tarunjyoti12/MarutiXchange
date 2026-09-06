package com.marutixchange.bidding_service.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis cache configuration for the bidding-service.
 *
 * Only active in production profile (--spring.profiles.active=prod).
 * In local/dev, Redis is not required — services start without it.
 *
 * Cache strategy:
 *   - auction-leaderboard : 8s TTL — evicted eagerly on every new bid
 *   - live-auctions       : 5s TTL — keeps BiddingPage list fresh
 */
@Configuration
@EnableCaching
@Profile("prod")   // Redis only in production — dev/local starts without Redis
public class RedisCacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
        cacheConfigs.put("auction-leaderboard", defaultConfig.entryTtl(Duration.ofSeconds(8)));
        cacheConfigs.put("live-auctions",        defaultConfig.entryTtl(Duration.ofSeconds(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigs)
                .build();
    }
}