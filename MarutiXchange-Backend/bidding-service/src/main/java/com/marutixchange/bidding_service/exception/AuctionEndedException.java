package com.marutixchange.bidding_service.exception;

public class AuctionEndedException extends RuntimeException {

    public AuctionEndedException(String message) {
        super(message);
    }

    public AuctionEndedException() {
        super("Auction has already ended");
    }
}