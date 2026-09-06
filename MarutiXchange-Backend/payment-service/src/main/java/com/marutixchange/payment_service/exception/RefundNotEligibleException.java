package com.marutixchange.payment_service.exception;
public class RefundNotEligibleException
        extends RuntimeException {
    public RefundNotEligibleException(
            String message) {
        super(message);
    }
}