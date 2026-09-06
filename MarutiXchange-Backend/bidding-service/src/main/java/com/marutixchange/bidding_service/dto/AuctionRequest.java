package com.marutixchange.bidding_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuctionRequest {

    @NotNull(message = "Car listing ID is required")
    private Long carListingId;

    @NotNull(message = "Car name is required")
    private String carName;

    private String carImageUrl;

    @NotNull(message = "Starting price is required")
    @Min(value = 10000, message = "Starting price too low")
    private Double startingPrice;

    private Double reservePrice;
    private Double buyNowPrice;

    @Min(value = 500, message = "Min increment too low")
    private Double minBidIncrement;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private String city;
    private String fuelType;
    private Integer year;
    private Integer mileage;
}