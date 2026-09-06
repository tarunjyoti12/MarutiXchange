package com.marutixchange.payment_service.exception;
public class PaymentAlreadyDoneException
        extends RuntimeException {
    public PaymentAlreadyDoneException(
            String message) {
        super(message);
    }
}