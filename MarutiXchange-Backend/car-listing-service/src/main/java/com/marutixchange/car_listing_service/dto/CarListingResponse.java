package com.marutixchange.car_listing_service.dto;

import com.marutixchange.car_listing_service.entity.CarListing;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CarListingResponse {

    private Long id;
    private Long sellerId;
    private String brand;
    private String model;
    private Integer year;
    private String variant;
    private String fuelType;
    private String transmission;
    private String color;
    private Integer mileage;
    private Integer engineCc;
    private Integer ownerNumber;
    private String registrationNumber;
    private Double askingPrice;
    private Double marketPrice;
    private String city;
    private String state;
    private String description;
    private String status;
    private Boolean isCertified;
    private Boolean isBoosted;
    private Integer viewCount;
    private Integer inquiryCount;
    private Boolean testDriveAvailable;
    private String insuranceValidTill;
    private List<String> imageUrls;
    private Double depreciationValue;
    private String priceRating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CarListingResponse fromEntity(CarListing car) {
        return CarListingResponse.builder()
                .id(car.getId())
                .sellerId(car.getSellerId())
                .brand(car.getBrand())
                .model(car.getModel())
                .year(car.getYear())
                .variant(car.getVariant())
                .fuelType(car.getFuelType() != null ? car.getFuelType().name() : null)
                .transmission(car.getTransmission() != null ? car.getTransmission().name() : null)
                .color(car.getColor())
                .mileage(car.getMileage())
                .engineCc(car.getEngineCc())
                .ownerNumber(car.getOwnerNumber())
                .registrationNumber(car.getRegistrationNumber())
                .askingPrice(car.getAskingPrice())
                .marketPrice(car.getMarketPrice())
                .city(car.getCity())
                .state(car.getState())
                .description(car.getDescription())
                .status(car.getStatus() != null ? car.getStatus().name() : null)
                .isCertified(car.getIsCertified())
                .isBoosted(car.getIsBoosted())
                .viewCount(car.getViewCount())
                .inquiryCount(car.getInquiryCount())
                .testDriveAvailable(car.getTestDriveAvailable())
                .insuranceValidTill(car.getInsuranceValidTill())
                .createdAt(car.getCreatedAt())
                .updatedAt(car.getUpdatedAt())
                .build();
    }
}