package com.marutixchange.bidding_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BidRequest {

    @NotNull(message = "Auction ID is required")
    private Long auctionId;

    private String bidderName;

    @NotNull(message = "Bid amount is required")
    @Min(value = 1000, message = "Bid amount too low")
    private Double bidAmount;
}