package com.marutixchange.payment_service.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private Long userId;
    private String channel;
    private String notificationType;
    private String title;
    private String body;
}