package com.marutixchange.car_listing_service.dto;

import com.marutixchange.car_listing_service.entity.CarListing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

@Data
public class CarListingRequest {

    @NotNull(message = "Seller ID is required")
    private Long sellerId;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotNull(message = "Year is required")
    @Min(value = 2000, message = "Year must be after 2000")
    @Max(value = 2026, message = "Invalid year")
    private Integer year;

    private String variant;

    @NotNull(message = "Fuel type is required")
    private CarListing.FuelType fuelType;

    @NotNull(message = "Transmission is required")
    private CarListing.Transmission transmission;

    private String color;
    private Integer mileage;
    private Integer engineCc;
    private Integer ownerNumber;
    private String registrationNumber;
    private String vinNumber;
    private String rcNumber;

    @NotNull(message = "Asking price is required")
    @Min(value = 50000, message = "Price too low")
    @Max(value = 10000000, message = "Price too high")
    private Double askingPrice;

    private String city;
    private String state;
    private String pincode;
    private String description;
    private Boolean testDriveAvailable;
    private String insuranceValidTill;
}