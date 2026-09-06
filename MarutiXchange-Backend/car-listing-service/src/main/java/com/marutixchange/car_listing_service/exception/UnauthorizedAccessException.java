package com.marutixchange.car_listing_service.exception;

public class UnauthorizedAccessException
        extends RuntimeException {
    public UnauthorizedAccessException() {
        super("You are not authorized to perform this action");
    }
}