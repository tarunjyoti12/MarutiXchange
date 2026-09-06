package com.marutixchange.payment_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "escrows")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Escrow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "escrow_reference",
            unique = true, nullable = false)
    private String escrowReference;

    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "held_amount", nullable = false)
    private Double heldAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EscrowStatus status =
            EscrowStatus.FUNDS_HELD;

    @Column(name = "delivery_confirmed_at")
    private LocalDateTime deliveryConfirmedAt;

    @Column(name = "funds_released_at")
    private LocalDateTime fundsReleasedAt;

    @Column(name = "auto_release_at")
    private LocalDateTime autoReleaseAt;

    @Column(name = "release_notes")
    private String releaseNotes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum EscrowStatus {
        BUYER_PAID,
        FUNDS_HELD,
        DELIVERY_ACTIVE,
        RELEASE_PENDING,
        FUNDS_RELEASED,
        AUTO_REFUNDED,
        DISPUTED
    }
}