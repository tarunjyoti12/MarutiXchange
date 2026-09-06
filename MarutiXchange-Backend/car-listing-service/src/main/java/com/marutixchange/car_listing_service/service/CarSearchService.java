package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.dto.CarSearchRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CarSearchService {
    Page<CarListingResponse> searchListings(
            CarSearchRequest request);
    List<CarListingResponse> getRecommendations(
            String brand, String city);
    List<CarListingResponse> compareListings(
            List<Long> listingIds);
}