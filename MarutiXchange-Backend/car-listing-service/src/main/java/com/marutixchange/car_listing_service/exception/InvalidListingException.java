package com.marutixchange.car_listing_service.exception;

public class InvalidListingException
        extends RuntimeException {
    public InvalidListingException(String message) {
        super(message);
    }
}