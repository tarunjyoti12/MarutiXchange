package com.marutixchange.notification_service.dto;

import lombok.Data;

@Data
public class RuleContext {

    private String type;
    private String notificationType;
    private String channel;
    private int hour;
    private int notificationCount;

    private boolean approved = true;
    private String message;
}