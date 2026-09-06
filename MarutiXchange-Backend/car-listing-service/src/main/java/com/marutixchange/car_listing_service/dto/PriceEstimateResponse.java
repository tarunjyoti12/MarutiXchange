package com.marutixchange.car_listing_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PriceEstimateResponse {

    private String brand;
    private String model;
    private Integer year;
    private Double marketPrice;
    private Double depreciatedValue;
    private Double minPrice;
    private Double maxPrice;
    private String priceRating;
    private String recommendation;
    private Double depreciationRate;
    private Integer age;
}