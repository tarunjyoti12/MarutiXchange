package com.marutixchange.bidding_service.kafka;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    private Long   userId;
    private String type;
    private String channel;
    private String title;
    private String body;
    private String referenceId;
    private String referenceType;
    private LocalDateTime createdAt;
}
