package com.marutixchange.rc_transfer_service.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class RcTransferEventProducer {

    private final KafkaTemplate<String, RcNotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.notification-events:notification-events}")
    private String notificationTopic;

    public void publishTransferInitiated(Long buyerId, Long sellerId,
                                          Long transferId, String regNumber) {
        publish(buyerId, "RC_TRANSFER_INITIATED",
                "RC Transfer Started",
                "RC transfer for vehicle " + regNumber + " has been initiated.",
                transferId);
        publish(sellerId, "RC_TRANSFER_INITIATED",
                "RC Transfer Started",
                "RC transfer for vehicle " + regNumber + " has been initiated by the buyer.",
                transferId);
    }

    public void publishRtoSubmitted(Long buyerId, Long sellerId,
                                     Long transferId, String rtoOffice) {
        String msg = "Documents submitted to " + rtoOffice + ". Processing takes ~21 days.";
        publish(buyerId,  "RC_RTO_SUBMITTED", "Documents Submitted to RTO", msg, transferId);
        publish(sellerId, "RC_RTO_SUBMITTED", "Documents Submitted to RTO", msg, transferId);
    }

    public void publishTransferCompleted(Long buyerId, Long sellerId,
                                          Long transferId, String regNumber) {
        publish(buyerId, "RC_TRANSFER_COMPLETED",
                "RC Transfer Completed ✅",
                "Ownership of vehicle " + regNumber + " has been successfully transferred to you.",
                transferId);
        publish(sellerId, "RC_TRANSFER_COMPLETED",
                "RC Transfer Completed",
                "RC transfer for vehicle " + regNumber + " is complete.",
                transferId);
    }

    private void publish(Long userId, String type, String title, String body, Long transferId) {
        RcNotificationEvent event = RcNotificationEvent.builder()
                .userId(userId).type(type).channel("PUSH")
                .title(title).body(body)
                .referenceId(String.valueOf(transferId))
                .referenceType("RC_TRANSFER")
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(notificationTopic, String.valueOf(userId), event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("❌ RC Transfer Kafka failed userId={}: {}", userId, ex.getMessage());
                    else
                        log.info("✅ RC Transfer Kafka sent | userId={} type={}", userId, type);
                });
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class RcNotificationEvent implements Serializable {
        private Long userId;
        private String type, channel, title, body, referenceId, referenceType;
        private LocalDateTime createdAt;
    }
}
