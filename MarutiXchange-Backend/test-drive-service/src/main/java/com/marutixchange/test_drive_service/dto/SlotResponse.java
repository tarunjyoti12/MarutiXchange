package com.marutixchange.test_drive_service.dto;

import com.marutixchange.test_drive_service.entity.TestDriveSlot;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class SlotResponse {

    private Long id;
    private Long listingId;
    private Long sellerId;
    private LocalDate slotDate;
    private LocalTime slotTime;
    private Boolean isBooked;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static SlotResponse fromEntity(TestDriveSlot slot) {
        return SlotResponse.builder()
                .id(slot.getId())
                .listingId(slot.getListingId())
                .sellerId(slot.getSellerId())
                .slotDate(slot.getSlotDate())
                .slotTime(slot.getSlotTime())
                .isBooked(slot.getIsBooked())
                .isActive(slot.getIsActive())
                .createdAt(slot.getCreatedAt())
                .build();
    }
}
