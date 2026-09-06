package com.marutixchange.test_drive_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Represents a time slot that a seller/dealer makes available
 * for test drives on a specific listing.
 */
@Entity
@Table(
    name = "test_drive_slots",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_slot_listing_date_time",
            columnNames = {"listing_id", "slot_date", "slot_time"}
        )
    },
    indexes = {
        @Index(name = "idx_slot_listing_date", columnList = "listing_id, slot_date"),
        @Index(name = "idx_slot_seller",        columnList = "seller_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDriveSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "slot_time", nullable = false)
    private LocalTime slotTime;

    @Builder.Default
    @Column(name = "is_booked", nullable = false)
    private Boolean isBooked = false;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    /** FK to test_drives.id when this slot gets booked */
    @Column(name = "booked_by_test_drive_id")
    private Long bookedByTestDriveId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
