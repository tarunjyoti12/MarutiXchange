package com.marutixchange.car_listing_service.dto;

import lombok.Data;

@Data
public class CarFilterRequest {

    private String brand;
    private String city;
    private Double minPrice;
    private Double maxPrice;
    private Integer yearFrom;
    private Integer yearTo;
}