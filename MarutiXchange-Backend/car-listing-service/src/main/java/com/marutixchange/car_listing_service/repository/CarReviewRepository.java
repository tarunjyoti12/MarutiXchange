package com.marutixchange.car_listing_service.repository;

import com.marutixchange.car_listing_service.entity.CarReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarReviewRepository
        extends JpaRepository<CarReview, Long> {

    // ✅ FIX
    List<CarReview> findByCarListing_Id(Long carListingId);
}