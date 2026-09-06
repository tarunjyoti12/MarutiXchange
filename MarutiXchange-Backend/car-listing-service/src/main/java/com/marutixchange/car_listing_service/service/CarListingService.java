package com.marutixchange.car_listing_service.service;

import com.marutixchange.car_listing_service.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CarListingService {
    CarListingResponse createListing(
            CarListingRequest request);
    CarListingResponse getListingById(Long id);
    CarListingResponse updateListing(
            Long id, CarListingRequest request);
    void deleteListing(Long id);
    void markAsSold(Long id);
    CarListingResponse uploadImages(
            Long id, List<MultipartFile> images)
            throws IOException;
    Page<CarListingResponse> getAllListings(
            int page, int size, String sortBy);
    List<CarListingResponse> getFeaturedListings();
    List<CarListingResponse> getSellerListings(
            Long sellerId);
    void boostListing(Long id);
    void incrementViewCount(Long id);
}