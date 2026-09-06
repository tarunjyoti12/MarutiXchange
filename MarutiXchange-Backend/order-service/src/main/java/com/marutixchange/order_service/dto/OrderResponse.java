package com.marutixchange.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String status;
    private String paymentId;
}