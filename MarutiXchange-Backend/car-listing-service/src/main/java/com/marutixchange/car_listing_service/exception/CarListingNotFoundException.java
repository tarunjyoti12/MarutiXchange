package com.marutixchange.car_listing_service.exception;

public class CarListingNotFoundException
        extends RuntimeException {
    public CarListingNotFoundException(Long id) {
        super("Car listing not found with id: " + id);
    }
}