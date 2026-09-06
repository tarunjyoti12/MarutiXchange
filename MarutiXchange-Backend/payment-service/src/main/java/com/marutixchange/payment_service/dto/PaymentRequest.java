package com.marutixchange.payment_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    @NotNull(message = "Car listing ID is required")
    private Long carListingId;

    private Long auctionId;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotNull(message = "Amount is required")
    @Min(value = 1000,
            message = "Amount must be at least 1000")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private String paymentMethod;

    private String paymentType;
    private String upiId;
    private String upiApp;
    private String bankName;
    private String idempotencyKey;
    private Boolean isTokenPayment;
}