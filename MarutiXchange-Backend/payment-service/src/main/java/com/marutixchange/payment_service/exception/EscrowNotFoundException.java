package com.marutixchange.payment_service.exception;
public class EscrowNotFoundException
        extends RuntimeException {
    public EscrowNotFoundException(String message) {
        super(message);
    }
}