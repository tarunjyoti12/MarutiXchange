package com.marutixchange.notification_service.service;

import com.marutixchange.notification_service.dto.NotificationRequest;
import com.marutixchange.notification_service.dto.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    NotificationResponse send(NotificationRequest request);

    // ✅ FIXED: UUID → Long
    Page<NotificationResponse> getByUserId(Long userId, Pageable pageable);

    NotificationResponse getById(UUID notificationId);

    void retryFailed();
}