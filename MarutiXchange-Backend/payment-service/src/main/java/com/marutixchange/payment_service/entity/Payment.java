package com.marutixchange.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id",
            unique = true, nullable = false)
    private String transactionId;

    @Column(name = "idempotency_key",
            unique = true)
    private String idempotencyKey;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "auction_id")
    private Long auctionId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "token_amount")
    @Builder.Default
    private Double tokenAmount = 10000.0;

    @Column(name = "remaining_amount")
    private Double remainingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentMethod paymentMethod =
            PaymentMethod.UPI;

    @Column(name = "upi_id")
    private String upiId;

    @Column(name = "upi_app")
    private String upiApp;

    @Column(name = "bank_name")
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status =
            PaymentStatus.INITIATED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentType paymentType =
            PaymentType.TOKEN;

    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;

    @Column(name = "payment_gateway_ref")
    private String paymentGatewayRef;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "is_token_payment")
    @Builder.Default
    private Boolean isTokenPayment = false;

    @Column(name = "timeout_at")
    private LocalDateTime timeoutAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PaymentMethod {
        UPI, CARD, NET_BANKING, CARD_EMI, NEFT, RTGS
    }

    public enum PaymentStatus {
        INITIATED,
        PENDING,
        SUCCESS,
        FAILED,
        TIMEOUT,
        REFUNDED,
        PARTIALLY_REFUNDED,
        CANCELLED
    }

    public enum PaymentType {
        TOKEN,
        FULL_PAYMENT,
        REMAINING_BALANCE,
        AUCTION_WINNING,
        BUY_NOW
    }
}