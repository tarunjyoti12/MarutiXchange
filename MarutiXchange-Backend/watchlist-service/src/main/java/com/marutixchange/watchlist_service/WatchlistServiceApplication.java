package com.marutixchange.watchlist_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
// @EnableCaching moved to RedisConfig with @Profile("prod") guard
// This prevents startup crash when Redis is not running locally
public class WatchlistServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(WatchlistServiceApplication.class, args);
    }
}