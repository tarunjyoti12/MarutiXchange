package com.marutixchange.car_listing_service.controller;

import com.marutixchange.car_listing_service.dto.ApiResponse;
import com.marutixchange.car_listing_service.dto.SellerAnalyticsResponse;
import com.marutixchange.car_listing_service.entity.CarListing;
import com.marutixchange.car_listing_service.repository.CarListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class SellerAnalyticsController {

    private final CarListingRepository listingRepository;

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<SellerAnalyticsResponse>> getSellerAnalytics(
            @PathVariable Long sellerId) {

        List<CarListing> listings =
                listingRepository.findBySellerId(sellerId);

        int total = listings.size();

        long active = listings.stream()
                .filter(l -> l.getStatus() == CarListing.ListingStatus.ACTIVE)
                .count();

        long sold = listings.stream()
                .filter(l -> l.getStatus() == CarListing.ListingStatus.SOLD)
                .count();

        int totalViews = listings.stream()
                .mapToInt(CarListing::getViewCount)
                .sum();

        int totalInquiries = listings.stream()
                .mapToInt(CarListing::getInquiryCount)
                .sum();

        String rating = total == 0 ? "NEW"
                : sold > total * 0.7 ? "EXCELLENT"
                : sold > total * 0.4 ? "GOOD"
                : "AVERAGE";

        SellerAnalyticsResponse response =
                SellerAnalyticsResponse.builder()
                        .sellerId(sellerId)
                        .totalListings(total)
                        .activeListings((int) active)
                        .soldListings((int) sold)
                        .totalViews(totalViews)
                        .totalInquiries(totalInquiries)
                        .performanceRating(rating)
                        .build();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Analytics fetched",
                        response));
    }
}