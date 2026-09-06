package com.marutixchange.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {

    private String orderId;
    private Integer amount;
    private String currency;
    private String receipt;
    private String keyId;
}