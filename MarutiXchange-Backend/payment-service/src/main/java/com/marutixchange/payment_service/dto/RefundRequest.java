package com.marutixchange.payment_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {

    @NotBlank(
            message = "Transaction ID is required")
    private String transactionId;

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    @NotNull(message = "Reason is required")
    private String reason;

    private String reasonDescription;
}