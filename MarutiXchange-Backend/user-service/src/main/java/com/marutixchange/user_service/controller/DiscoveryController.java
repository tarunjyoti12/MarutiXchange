package com.marutixchange.user_service.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@Slf4j
public class DiscoveryController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        log.info("User Service health requested");

        Map<String, Object> response = new HashMap<>();
        response.put("service", "user-service");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "1.0.0");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getInfo() {
        log.info("User Service info requested");

        Map<String, Object> info = new HashMap<>();
        info.put("service", "user-service");
        info.put("description", "User Management Service for MarutiXchange");
        info.put("features", new String[]{
                "User Registration",
                "JWT Authentication",
                "OTP Verification",
                "Password Reset",
                "Document Upload"
        });

        return ResponseEntity.ok(info);
    }
}