package com.marutixchange.test_drive_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TestDriveRequest {

    @NotNull(message = "buyerId is required")
    private Long buyerId;

    @NotNull(message = "listingId is required")
    private Long listingId;

    @NotNull(message = "slotDate is required")
    @Future(message = "Slot date must be in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate slotDate;

    @NotNull(message = "slotTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime slotTime;

    @Size(max = 500, message = "Address too long")
    private String locationAddress;

    @Size(max = 100)
    private String locationCity;

    @Pattern(regexp = "^[0-9]{6}$", message = "Invalid pincode")
    private String locationPincode;
}
