package com.marutixchange.payment_service.kafka;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Kafka producer for payment-service.
 * Replaces direct HTTP calls to notification-service with async Kafka events.
 * Used in PaymentServiceImpl after payment success/failure.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, PaymentNotificationEvent> kafkaTemplate;

    @Value("${kafka.topics.payment-events:payment-events}")
    private String paymentEventsTopic;

    public void publishPaymentSuccess(Long buyerId, Long sellerId, String transactionId,
                                      Double amount, Long carListingId) {
        publishToUser(buyerId, "PAYMENT_SUCCESS",
                "Payment Successful ✅",
                "Your payment of ₹" + String.format("%.2f", amount / 100000) + " L was successful. Txn: " + transactionId,
                transactionId, "PAYMENT");

        publishToUser(sellerId, "PAYMENT_RECEIVED",
                "Payment Received 💰",
                "You received a payment of ₹" + String.format("%.2f", amount / 100000) + " L for your listing.",
                transactionId, "PAYMENT");
    }

    public void publishPaymentFailed(Long buyerId, String transactionId, String reason) {
        publishToUser(buyerId, "PAYMENT_FAILED",
                "Payment Failed ❌",
                "Your payment failed: " + reason + ". Please try again.",
                transactionId, "PAYMENT");
    }

    public void publishRefundInitiated(Long buyerId, String transactionId, Double amount) {
        publishToUser(buyerId, "REFUND_INITIATED",
                "Refund Initiated",
                "Refund of ₹" + String.format("%.2f", amount / 100000) + " L initiated. Takes 3-5 business days.",
                transactionId, "REFUND");
    }

    private void publishToUser(Long userId, String type, String title, String body,
                                String referenceId, String referenceType) {
        PaymentNotificationEvent event = PaymentNotificationEvent.builder()
                .userId(userId)
                .type(type)
                .channel("PUSH")
                .title(title)
                .body(body)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .createdAt(LocalDateTime.now())
                .build();

        kafkaTemplate.send(paymentEventsTopic, String.valueOf(userId), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("❌ Payment Kafka event failed for userId={}: {}", userId, ex.getMessage());
                    } else {
                        log.info("✅ Payment Kafka event sent | userId={} type={}", userId, type);
                    }
                });
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PaymentNotificationEvent implements Serializable {
        private Long userId;
        private String type;
        private String channel;
        private String title;
        private String body;
        private String referenceId;
        private String referenceType;
        private LocalDateTime createdAt;
    }
}
