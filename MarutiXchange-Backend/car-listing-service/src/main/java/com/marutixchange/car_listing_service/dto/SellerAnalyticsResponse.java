package com.marutixchange.car_listing_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor   // ✅ IMPORTANT FIX
public class SellerAnalyticsResponse {

    private Long sellerId;
    private Integer totalListings;
    private Integer activeListings;
    private Integer soldListings;
    private Integer totalViews;
    private Integer totalInquiries;
    private Double averageDaysToSell;
    private Double averageSellingPrice;
    private String performanceRating;
}