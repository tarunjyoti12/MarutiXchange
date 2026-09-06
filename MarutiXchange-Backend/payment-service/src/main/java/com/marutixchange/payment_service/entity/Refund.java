package com.marutixchange.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "refunds")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "refund_reference",
            unique = true, nullable = false)
    private String refundReference;

    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "original_amount", nullable = false)
    private Double originalAmount;

    @Column(name = "refund_amount", nullable = false)
    private Double refundAmount;

    @Column(name = "cancellation_fee")
    @Builder.Default
    private Double cancellationFee = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundReason reason;

    @Column(name = "reason_description")
    private String reasonDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RefundStatus status =
            RefundStatus.INITIATED;

    @Column(name = "is_within_free_window")
    private Boolean isWithinFreeWindow;

    @Column(name = "hours_since_payment")
    private Long hoursSincePayment;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum RefundReason {
        CHANGED_MIND,
        SELLER_NOT_RESPONDING,
        FOUND_BETTER_DEAL,
        CAR_CONDITION_MISMATCH,
        FINANCING_NOT_APPROVED,
        SELLER_CANCELLED,
        FRAUD_DETECTED,
        OTHER
    }

    public enum RefundStatus {
        INITIATED,
        PROCESSING,
        COMPLETED,
        REJECTED,
        FAILED
    }
}