package com.marutixchange.test_drive_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
    name = "test_drives",
    indexes = {
        @Index(name = "idx_td_buyer_id",    columnList = "buyer_id"),
        @Index(name = "idx_td_listing_id",  columnList = "listing_id"),
        @Index(name = "idx_td_status",      columnList = "status"),
        @Index(name = "idx_td_slot_date",   columnList = "slot_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── Participants ────────────────────────────────────────────────────────

    @NotNull
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @NotNull
    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    /** Seller / dealer who owns the listing */
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    // ─── Slot ────────────────────────────────────────────────────────────────

    @NotNull
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @NotNull
    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    /** Duration in minutes (default 30, configurable) */
    @Builder.Default
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes = 30;

    // ─── Location ────────────────────────────────────────────────────────────

    @Column(name = "location_address", length = 500)
    private String locationAddress;

    @Column(name = "location_city", length = 100)
    private String locationCity;

    @Column(name = "location_pincode", length = 10)
    private String locationPincode;

    // ─── Status ──────────────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private TestDriveStatus status = TestDriveStatus.PENDING;

    // ─── Feedback ────────────────────────────────────────────────────────────

    @Column(name = "buyer_rating")
    private Integer buyerRating;          // 1–5

    @Column(name = "buyer_feedback", length = 1000)
    private String buyerFeedback;

    @Column(name = "seller_notes", length = 1000)
    private String sellerNotes;

    // ─── Cancellation ────────────────────────────────────────────────────────

    @Column(name = "cancelled_by")
    private String cancelledBy;           // BUYER | SELLER | SYSTEM

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // ─── Completion ──────────────────────────────────────────────────────────

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ─── Audit ───────────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ─── Enum ─────────────────────────────────────────────────────────────────

    public enum TestDriveStatus {
        PENDING,      // Buyer requested, awaiting seller confirmation
        CONFIRMED,    // Seller confirmed the slot
        COMPLETED,    // Test drive happened
        CANCELLED,    // Cancelled by buyer / seller / system
        NO_SHOW       // Buyer didn't show up
    }
}
