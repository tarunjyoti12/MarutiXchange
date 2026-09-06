package com.marutixchange.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    private Long carListingId;   // ✅ MUST MATCH payment-service
    private Long buyerId;        // ✅ REQUIRED
    private Long sellerId;       // ✅ REQUIRED
    private Double amount;
    private String paymentMethod; // ✅ REQUIRED
}