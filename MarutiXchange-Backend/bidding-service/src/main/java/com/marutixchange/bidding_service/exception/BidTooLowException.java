package com.marutixchange.bidding_service.exception;

public class BidTooLowException extends RuntimeException {

    public BidTooLowException(String message) {
        super(message);
    }

    public BidTooLowException() {
        super("Bid amount is too low");
    }
}