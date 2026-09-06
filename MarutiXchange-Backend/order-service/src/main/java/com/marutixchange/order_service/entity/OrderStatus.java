package com.marutixchange.order_service.entity;

public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    FAILED,
    CANCELLED,   // ✅ ADD if missing
    COMPLETED
}