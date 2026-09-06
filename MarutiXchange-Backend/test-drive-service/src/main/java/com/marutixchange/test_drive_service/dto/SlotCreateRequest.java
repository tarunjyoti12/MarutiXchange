package com.marutixchange.test_drive_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class SlotCreateRequest {

    @NotNull(message = "listingId is required")
    private Long listingId;

    @NotNull(message = "sellerId is required")
    private Long sellerId;

    @NotNull(message = "slotDate is required")
    @Future(message = "Slot date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;

    /** Multiple time slots can be created for a single date in one call */
    @NotEmpty(message = "At least one slot time is required")
    @JsonFormat(pattern = "HH:mm")
    private List<LocalTime> slotTimes;
}
