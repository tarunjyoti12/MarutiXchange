package com.marutixchange.bidding_service.exception;

public class AuctionNotFoundException extends RuntimeException {

    public AuctionNotFoundException(Long id) {
        super("Auction not found with id: " + id);
    }

    public AuctionNotFoundException(String message) {
        super(message);
    }
}