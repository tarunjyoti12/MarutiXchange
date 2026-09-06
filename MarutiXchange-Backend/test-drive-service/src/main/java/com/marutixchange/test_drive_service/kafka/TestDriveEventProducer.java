package com.marutixchange.test_drive_service.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Kafka producer for test-drive-service.
 * Replaces direct Feign calls to notification-service with async Kafka events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDriveEventProducer {

    private final KafkaTemplate<String, TestDriveNotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.test-drive-events:test-drive-events}")
    private String testDriveTopic;

    public void publishTestDriveBooked(Long buyerId, Long sellerId, String carName,
                                        String date, String timeSlot, Long testDriveId) {
        // Notify buyer
        publish(buyerId, "TEST_DRIVE_BOOKED",
                "Test Drive Confirmed 🚗",
                "Your test drive for " + carName + " on " + date + " at " + timeSlot + " is confirmed.",
                String.valueOf(testDriveId), "TEST_DRIVE");

        // Notify seller
        publish(sellerId, "TEST_DRIVE_REQUEST",
                "New Test Drive Request",
                "A buyer has booked a test drive for " + carName + " on " + date + " at " + timeSlot + ".",
                String.valueOf(testDriveId), "TEST_DRIVE");
    }

    public void publishTestDriveCancelled(Long userId, String carName, Long testDriveId) {
        publish(userId, "TEST_DRIVE_CANCELLED",
                "Test Drive Cancelled",
                "Your test drive for " + carName + " has been cancelled.",
                String.valueOf(testDriveId), "TEST_DRIVE");
    }

    private void publish(Long userId, String type, String title, String body,
                          String referenceId, String referenceType) {
        TestDriveNotificationEvent event = TestDriveNotificationEvent.builder()
                .userId(userId).type(type).channel("PUSH")
                .title(title).body(body)
                .referenceId(referenceId).referenceType(referenceType)
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(testDriveTopic, String.valueOf(userId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null)
                        log.error("❌ TestDrive Kafka failed userId={}: {}", userId, ex.getMessage());
                    else
                        log.info("✅ TestDrive Kafka sent | userId={} type={}", userId, type);
                });
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TestDriveNotificationEvent implements Serializable {
        private Long userId;
        private String type, channel, title, body, referenceId, referenceType;
        private LocalDateTime createdAt;
    }
}
