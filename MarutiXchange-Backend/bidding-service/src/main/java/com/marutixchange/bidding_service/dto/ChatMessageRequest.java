package com.marutixchange.bidding_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotNull(message = "Car listing ID is required")
    private Long carListingId;

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotBlank(message = "Message is required")
    private String message;

    private String messageType;
    private Double offerAmount;
}