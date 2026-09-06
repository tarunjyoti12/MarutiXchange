package com.marutixchange.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private Long userId;
    private String notificationType;
    private String channel;
    private String title;
    private String body;
}
