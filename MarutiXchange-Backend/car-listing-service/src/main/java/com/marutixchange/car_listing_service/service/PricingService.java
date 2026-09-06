package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.dto.PriceEstimateResponse;

public interface PricingService {
    PriceEstimateResponse getEstimate(
            String brand, String model,
            Integer year, Integer mileage);
}