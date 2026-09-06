package com.marutixchange.bidding_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TestDriveRequest {

    @NotNull(message = "Car listing ID is required")
    private Long carListingId;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotNull(message = "Date is required")
    private LocalDate scheduledDate;

    @NotNull(message = "Time is required")
    private LocalTime scheduledTime;

    @NotNull(message = "Location is required")
    private String location;

    private String notes;
}