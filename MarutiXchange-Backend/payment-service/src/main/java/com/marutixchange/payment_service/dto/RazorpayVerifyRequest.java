package com.marutixchange.payment_service.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RazorpayVerifyRequest {
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;
    private Long carListingId;
    private Long buyerId;
    private Long sellerId;
    private Integer amount;
}