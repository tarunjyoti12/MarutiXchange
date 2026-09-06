package com.marutixchange.order_service.exception;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}