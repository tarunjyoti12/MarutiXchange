package com.marutixchange.notification_service.dto;

import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationStatus;
import com.marutixchange.notification_service.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private UUID notificationId;

    // ✅ FIXED: UUID → Long
    private Long userId;

    private NotificationType notificationType;
    private NotificationChannel channel;
    private NotificationStatus status;
    private String title;
    private String body;
    private UUID referenceId;
    private String referenceType;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}