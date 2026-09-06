package com.marutixchange.notification_service.kafka;

import lombok.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Kafka event payload for all notification events.
 * Produced by: bidding-service, car-listing-service, payment-service, test-drive-service
 * Consumed by: notification-service
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {

    private Long   userId;
    private String type;           // BID_PLACED, AUCTION_WON, LISTING_APPROVED, etc.
    private String channel;        // PUSH, EMAIL, SMS
    private String title;
    private String body;
    private String referenceId;    // auctionId, listingId, orderId, etc.
    private String referenceType;  // AUCTION, LISTING, ORDER, TEST_DRIVE
    private LocalDateTime createdAt;
}
