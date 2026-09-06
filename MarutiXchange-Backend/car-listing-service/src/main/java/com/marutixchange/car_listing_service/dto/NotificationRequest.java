package com.marutixchange.car_listing_service.dto;

import lombok.Data;

@Data
public class NotificationRequest {

    private Long userId;
    private String channel;           // ✅ ADDED
    private String notificationType;  // ✅ ADDED
    private String title;             // ✅ ADDED
    private String body;              // ✅ ADDED
}