package com.marutixchange.test_drive_service.dto;

import lombok.Data;

/**
 * Minimal projection of car-listing-service response.
 * Only fields needed by test-drive-service are mapped here.
 * Wrapped inside ApiResponse<CarListingClientResponse> from car-listing-service.
 */
@Data
public class CarListingClientResponse {

    private boolean success;
    private String message;
    private ListingData data;

    @Data
    public static class ListingData {
        private Long id;
        private Long sellerId;
        private String brand;
        private String model;
        private Integer year;
        private String status;           // ACTIVE, PENDING, SOLD, etc.
        private Boolean testDriveAvailable;
        private String city;
        private String state;
    }
}
