package com.marutixchange.payment_service.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RazorpayOrderRequest {
    private Long carListingId;
    private Long buyerId;
    private Long sellerId;
    private Integer amount; // in paise
    private String currency;
    private String receipt;
}