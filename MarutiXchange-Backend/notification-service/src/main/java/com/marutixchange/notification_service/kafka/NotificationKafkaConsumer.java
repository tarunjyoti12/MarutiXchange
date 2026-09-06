package com.marutixchange.notification_service.kafka;

import com.marutixchange.notification_service.dto.NotificationRequest;
import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationType;
import com.marutixchange.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for notification events.
 *
 * Only active in production profile (--spring.profiles.active=prod).
 * In local/dev, Kafka is not required — service starts without it.
 *
 * Supports 30L+ users via Kafka's horizontal scalability.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Profile("prod")   // Kafka only in production — dev/local starts without Kafka
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
        topics       = "${kafka.topics.notification-events}",
        groupId      = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeNotificationEvent(
            @Payload  NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC)     String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int    partition,
            @Header(KafkaHeaders.OFFSET)             long   offset
    ) {
        log.info("Kafka event received | topic={} partition={} offset={} userId={} type={}",
                topic, partition, offset, event.getUserId(), event.getType());

        try {
            NotificationRequest request = NotificationRequest.builder()
                    .userId(event.getUserId())
                    .notificationType(NotificationType.valueOf(event.getType()))
                    .channel(resolveChannel(event.getChannel()))
                    .title(event.getTitle())
                    .body(event.getBody())
                    .referenceType(event.getReferenceType())
                    .build();

            notificationService.send(request);
            log.info("Notification processed for userId={}", event.getUserId());

        } catch (Exception ex) {
            log.error("Failed to process notification event for userId={}: {}",
                    event.getUserId(), ex.getMessage(), ex);
            throw ex;
        }
    }

    @KafkaListener(
        topics       = "${kafka.topics.bid-events}",
        groupId      = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeBidEvent(
            @Payload  NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        log.info("Bid event received | topic={} userId={} type={}",
                topic, event.getUserId(), event.getType());
        consumeNotificationEvent(event, topic, 0, 0);
    }

    @KafkaListener(
        topics       = "${kafka.topics.auction-events}",
        groupId      = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAuctionEvent(
            @Payload  NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        log.info("Auction event received | topic={} userId={} type={}",
                topic, event.getUserId(), event.getType());
        consumeNotificationEvent(event, topic, 0, 0);
    }

    @KafkaListener(
        topics       = "${kafka.topics.payment-events}",
        groupId      = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePaymentEvent(
            @Payload  NotificationEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        log.info("Payment event received | topic={} userId={} type={}",
                topic, event.getUserId(), event.getType());
        consumeNotificationEvent(event, topic, 0, 0);
    }

    private NotificationChannel resolveChannel(String channel) {
        try {
            return NotificationChannel.valueOf(channel);
        } catch (Exception e) {
            return NotificationChannel.PUSH;
        }
    }
}