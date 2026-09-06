package com.marutixchange.car_listing_service.dto;

import com.marutixchange.car_listing_service.entity.CarListing;
import lombok.Data;

@Data
public class CarSearchRequest {

    private String brand;
    private String model;
    private Integer yearFrom;
    private Integer yearTo;
    private Double priceFrom;
    private Double priceTo;
    private CarListing.FuelType fuelType;
    private CarListing.Transmission transmission;
    private String city;
    private Integer maxMileage;
    private Integer ownerNumber;
    private String sortBy;
    private String sortDirection;

    private int page = 0;   // ✅ FIX
    private int size = 10;  // ✅ FIX
}