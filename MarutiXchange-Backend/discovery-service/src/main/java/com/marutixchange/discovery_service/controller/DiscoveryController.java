package com.marutixchange.discovery_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/discovery")
@Slf4j
public class DiscoveryController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        log.info("Discovery status requested");
        Map<String, Object> status = new HashMap<>();
        status.put("service",
                "MarutiXchange Discovery Service");
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("version", "1.0.0");
        status.put("description",
                "Eureka Server for MarutiXchange");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.info("Discovery info requested");
        Map<String, Object> info = new HashMap<>();
        info.put("platform", "MarutiXchange");
        info.put("service", "Discovery Service");
        info.put("port", 8761);
        info.put("eureka-dashboard",
                "http://localhost:8761");

        Map<String, Object> services = new HashMap<>();

// ✅ Completed Services
        services.put("user-service",
                Map.of("port", 8081, "status", "UP"));

        services.put("car-listing-service",
                Map.of("port", 8068, "status", "UP"));

        services.put("payment-service",
                Map.of("port", 8069, "status", "UP"));

// ⬜ Pending Services
        services.put("bidding-service",
                Map.of("port", 8083, "status", "PENDING"));

        services.put("booking-service",
                Map.of("port", 8084, "status", "PENDING"));

        services.put("notification-service",
                Map.of("port", 8085, "status", "PENDING"));

        services.put("api-gateway",
                Map.of("port", 8090, "status", "PENDING"));

        info.put("registered-services", services);
        return ResponseEntity.ok(info);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.info("Discovery health requested");
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "discovery-service");
        health.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(health);
    }

    // ✅ NEW - Stats API
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("Discovery stats requested");
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalServices", 7);
        stats.put("completedServices", 2);
        stats.put("pendingServices", 5);
        stats.put("platformHealth", "GOOD");
        stats.put("lastChecked", LocalDateTime.now());

        Map<String, String> completedList =
                new HashMap<>();
        completedList.put("user-service", "✅ Complete");
        completedList.put("discovery-service",
                "✅ Complete");
        stats.put("completed", completedList);

        Map<String, String> pendingList = new HashMap<>();
        pendingList.put("car-listing-service",
                "⬜ Pending");
        pendingList.put("bidding-service", "⬜ Pending");
        pendingList.put("booking-service", "⬜ Pending");
        pendingList.put("notification-service",
                "⬜ Pending");
        pendingList.put("api-gateway", "⬜ Pending");
        stats.put("pending", pendingList);

        return ResponseEntity.ok(stats);
    }
}