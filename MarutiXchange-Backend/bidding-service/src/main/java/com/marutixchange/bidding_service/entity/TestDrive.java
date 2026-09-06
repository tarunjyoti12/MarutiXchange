package com.marutixchange.bidding_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "test_drives",
        indexes = {
                @Index(name = "idx_testdrive_buyer", columnList = "buyer_id"),
                @Index(name = "idx_testdrive_seller", columnList = "seller_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestDrive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(nullable = false)
    private String location;

    private String notes;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TestDriveStatus status = TestDriveStatus.REQUESTED;

    private String cancellationReason;

    @CreationTimestamp
    private LocalDateTime createdAt;

    public enum TestDriveStatus {
        REQUESTED,
        CONFIRMED,
        COMPLETED,
        CANCELLED,
        RESCHEDULED
    }
}