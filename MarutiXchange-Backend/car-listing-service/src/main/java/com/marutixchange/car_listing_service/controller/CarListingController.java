package com.marutixchange.car_listing_service.controller;

import com.marutixchange.car_listing_service.dto.ApiResponse;
import com.marutixchange.car_listing_service.dto.CarListingRequest;
import com.marutixchange.car_listing_service.dto.CarListingResponse;
import com.marutixchange.car_listing_service.service.CarListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class CarListingController {

    private final CarListingService listingService;

    @PostMapping
    public ResponseEntity<ApiResponse<CarListingResponse>> createListing(
            @Valid @RequestBody CarListingRequest request) {

        CarListingResponse response =
                listingService.createListing(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Listing created successfully",
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CarListingResponse>> getListingById(
            @PathVariable Long id) {

        CarListingResponse response =
                listingService.getListingById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listing fetched successfully",
                        response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CarListingResponse>> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody CarListingRequest request) {

        CarListingResponse response =
                listingService.updateListing(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listing updated successfully",
                        response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteListing(
            @PathVariable Long id) {

        listingService.deleteListing(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listing deleted successfully",
                        null));
    }

    @PatchMapping("/{id}/sold")
    public ResponseEntity<ApiResponse<Void>> markAsSold(
            @PathVariable Long id) {

        listingService.markAsSold(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listing marked as sold",
                        null));
    }

    @PostMapping("/{id}/images")
    public ResponseEntity<ApiResponse<CarListingResponse>> uploadImages(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images)
            throws IOException {

        CarListingResponse response =
                listingService.uploadImages(id, images);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Images uploaded successfully",
                        response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CarListingResponse>>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        Page<CarListingResponse> listings =
                listingService.getAllListings(page, size, sortBy);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listings fetched successfully",
                        listings));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<CarListingResponse>>> getFeaturedListings() {

        List<CarListingResponse> listings =
                listingService.getFeaturedListings();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Featured listings fetched",
                        listings));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<ApiResponse<List<CarListingResponse>>> getSellerListings(
            @PathVariable Long sellerId) {

        List<CarListingResponse> listings =
                listingService.getSellerListings(sellerId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller listings fetched",
                        listings));
    }

    @PatchMapping("/{id}/boost")
    public ResponseEntity<ApiResponse<Void>> boostListing(
            @PathVariable Long id) {

        listingService.boostListing(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Listing boosted successfully",
                        null));
    }
}
