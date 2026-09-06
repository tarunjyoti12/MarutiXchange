package com.marutixchange.payment_service.exception;
public class PaymentValidationException
        extends RuntimeException {
    public PaymentValidationException(
            String message) {
        super(message);
    }
}