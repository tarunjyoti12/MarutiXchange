package com.marutixchange.notification_service.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * REST client to fetch user contact info (email, phone) from User Microservice.
 * Mirrors the RuleEngineClient pattern in Payment Service.
 */
@Component
@Slf4j
public class UserServiceClient {

    private final RestTemplate restTemplate;

    @Value("${service.user-service.url:http://user-service}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getUserContactInfo(UUID userId) {
        try {
            String url = userServiceUrl + "/api/users/" + userId + "/contact";
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.warn("Failed to fetch contact info for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }
}