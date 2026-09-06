package com.marutixchange.notification_service.controller;

import com.marutixchange.notification_service.dto.ApiResponse;
import com.marutixchange.notification_service.dto.NotificationRequest;
import com.marutixchange.notification_service.dto.NotificationResponse;
import com.marutixchange.notification_service.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "Send a notification manually (admin/internal use)")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @Valid @RequestBody NotificationRequest request) {

        log.info("🔥 Notification received: {}", request);

        NotificationResponse response = notificationService.send(request);

        log.info("✅ Notification processed successfully");

        return ResponseEntity.ok(
                ApiResponse.<NotificationResponse>builder()
                        .success(true)
                        .message("Notification sent successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get paginated notification history for a user")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getByUser(
            // ✅ FIXED: UUID → Long
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<NotificationResponse> result =
                notificationService.getByUserId(userId, PageRequest.of(page, size));

        return ResponseEntity.ok(
                ApiResponse.<Page<NotificationResponse>>builder()
                        .success(true)
                        .message("Notifications fetched")
                        .data(result)
                        .build());
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "Get a notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getById(
            @PathVariable UUID notificationId) {

        NotificationResponse response = notificationService.getById(notificationId);

        return ResponseEntity.ok(
                ApiResponse.<NotificationResponse>builder()
                        .success(true)
                        .message("Notification fetched")
                        .data(response)
                        .build());
    }
}