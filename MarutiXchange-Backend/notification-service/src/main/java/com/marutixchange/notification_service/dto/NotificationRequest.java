package com.marutixchange.notification_service.dto;

import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "notificationType is required")
    private NotificationType notificationType;

    @NotNull(message = "channel is required")
    private NotificationChannel channel;

    @NotBlank(message = "title is required")
    private String title;

    @NotBlank(message = "body is required")
    private String body;

    private UUID referenceId;
    private String referenceType;
    private List<UUID> additionalUserIds;
}