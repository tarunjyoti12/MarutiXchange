package com.marutixchange.bidding_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BuyNowRequest {

    @NotNull(message = "Auction ID is required")
    private Long auctionId;

    private String buyerName;
}