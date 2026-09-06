package com.marutixchange.bidding_service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for bidding-service.
 * Replaces direct HTTP call to notification-service with async Kafka events.
 * Used in BidServiceImpl and AuctionServiceImpl.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BidEventProducer {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.bid-events:bid-events}")
    private String bidEventsTopic;

    @Value("${kafka.topics.auction-events:auction-events}")
    private String auctionEventsTopic;

    /**
     * Publish bid-placed notification asynchronously via Kafka.
     * Replaces: restTemplate.postForEntity(notificationUrl, ...)
     */
    public void publishBidPlaced(Long bidderId, Long auctionId, Double bidAmount) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(bidderId)
                .type("BID_PLACED")
                .channel("PUSH")
                .title("Bid Placed Successfully")
                .body("Your bid of ₹" + String.format("%.2f", bidAmount / 100000) + " L was placed.")
                .referenceId(String.valueOf(auctionId))
                .referenceType("AUCTION")
                .createdAt(LocalDateTime.now())
                .build();

        sendAsync(bidEventsTopic, String.valueOf(bidderId), event);
    }

    public void publishBidOutbid(Long outbidUserId, Long auctionId, Double newHighestBid) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(outbidUserId)
                .type("BID_OUTBID")
                .channel("PUSH")
                .title("You've been outbid!")
                .body("Someone placed a higher bid of ₹"
                        + String.format("%.2f", newHighestBid / 100000) + " L. Bid again to stay ahead.")
                .referenceId(String.valueOf(auctionId))
                .referenceType("AUCTION")
                .createdAt(LocalDateTime.now())
                .build();

        sendAsync(bidEventsTopic, String.valueOf(outbidUserId), event);
    }

    public void publishAuctionWon(Long winnerId, Long auctionId, String carName) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(winnerId)
                .type("AUCTION_WON")
                .channel("PUSH")
                .title("🎉 Congratulations! You won the auction!")
                .body("You won the auction for " + carName + ". Proceed to payment to complete your purchase.")
                .referenceId(String.valueOf(auctionId))
                .referenceType("AUCTION")
                .createdAt(LocalDateTime.now())
                .build();

        sendAsync(auctionEventsTopic, String.valueOf(winnerId), event);
    }

    public void publishAuctionEnded(Long sellerId, Long auctionId, String carName) {
        NotificationEvent event = NotificationEvent.builder()
                .userId(sellerId)
                .type("AUCTION_ENDED")
                .channel("PUSH")
                .title("Auction Ended")
                .body("Your auction for " + carName + " has ended.")
                .referenceId(String.valueOf(auctionId))
                .referenceType("AUCTION")
                .createdAt(LocalDateTime.now())
                .build();

        sendAsync(auctionEventsTopic, String.valueOf(sellerId), event);
    }

    private void sendAsync(String topic, String key, NotificationEvent event) {
        CompletableFuture<SendResult<String, NotificationEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("❌ Kafka send failed | topic={} key={} type={} error={}",
                        topic, key, event.getType(), ex.getMessage());
            } else {
                log.info("✅ Kafka event sent | topic={} partition={} offset={} type={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getType());
            }
        });
    }
}
