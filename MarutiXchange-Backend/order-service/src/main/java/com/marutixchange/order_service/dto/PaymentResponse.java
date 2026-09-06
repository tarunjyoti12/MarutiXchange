package com.marutixchange.order_service.dto;

import lombok.Data;

@Data
public class PaymentResponse {

    private String paymentId;

    // ✅ ADD THIS (IMPORTANT)
    private String transactionId;

    private String status;
}