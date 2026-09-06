package com.marutixchange.test_drive_service.dto;

import com.marutixchange.test_drive_service.entity.TestDrive;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class TestDriveResponse {

    private Long id;
    private Long buyerId;
    private Long listingId;
    private Long sellerId;

    private LocalDate slotDate;
    private LocalTime slotTime;
    private Integer durationMinutes;

    private String locationAddress;
    private String locationCity;
    private String locationPincode;

    private String status;

    private Integer buyerRating;
    private String buyerFeedback;
    private String sellerNotes;

    private String cancelledBy;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime completedAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TestDriveResponse fromEntity(TestDrive td) {
        return TestDriveResponse.builder()
                .id(td.getId())
                .buyerId(td.getBuyerId())
                .listingId(td.getListingId())
                .sellerId(td.getSellerId())
                .slotDate(td.getSlotDate())
                .slotTime(td.getSlotTime())
                .durationMinutes(td.getDurationMinutes())
                .locationAddress(td.getLocationAddress())
                .locationCity(td.getLocationCity())
                .locationPincode(td.getLocationPincode())
                .status(td.getStatus() != null ? td.getStatus().name() : null)
                .buyerRating(td.getBuyerRating())
                .buyerFeedback(td.getBuyerFeedback())
                .sellerNotes(td.getSellerNotes())
                .cancelledBy(td.getCancelledBy())
                .cancellationReason(td.getCancellationReason())
                .cancelledAt(td.getCancelledAt())
                .completedAt(td.getCompletedAt())
                .createdAt(td.getCreatedAt())
                .updatedAt(td.getUpdatedAt())
                .build();
    }
}
