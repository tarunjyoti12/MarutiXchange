package com.marutixchange.car_listing_service.controller;

import com.marutixchange.car_listing_service.dto.ApiResponse;
import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.dto.CarSearchRequest;
import com.marutixchange.car_listing_service.dto.PriceEstimateResponse;
import com.marutixchange.car_listing_service.service.CarSearchService;
import com.marutixchange.car_listing_service.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class CarSearchController {

    private final CarSearchService searchService;
    private final PricingService pricingService;

    @PostMapping
    public ResponseEntity<ApiResponse<Page<CarListingResponse>>> search(
            @RequestBody CarSearchRequest request) {

        Page<CarListingResponse> results =
                searchService.searchListings(request);

        return ResponseEntity.ok(
                ApiResponse.success("Search results", results));
    }

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<CarListingResponse>>> getRecommendations(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city) {

        List<CarListingResponse> recommendations =
                searchService.getRecommendations(brand, city);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Recommendations fetched",
                        recommendations));
    }

    @PostMapping("/compare")
    public ResponseEntity<ApiResponse<List<CarListingResponse>>> compare(
            @RequestBody List<Long> listingIds) {

        List<CarListingResponse> comparison =
                searchService.compareListings(listingIds);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Comparison results",
                        comparison));
    }

    @GetMapping("/price-estimate")
    public ResponseEntity<ApiResponse<PriceEstimateResponse>> getPriceEstimate(
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam(required = false) Integer mileage) {

        PriceEstimateResponse estimate =
                pricingService.getEstimate(
                        brand, model, year, mileage);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Price estimate fetched",
                        estimate));
    }
}