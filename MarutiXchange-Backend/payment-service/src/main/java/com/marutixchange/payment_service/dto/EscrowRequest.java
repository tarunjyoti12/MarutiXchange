package com.marutixchange.payment_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EscrowRequest {

    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Car listing ID is required")
    private Long carListingId;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotNull(message = "Amount is required")
    private Double heldAmount;
}