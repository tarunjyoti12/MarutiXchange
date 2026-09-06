package com.marutixchange.car_listing_service.repository;

import com.marutixchange.car_listing_service.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarImageRepository
        extends JpaRepository<CarImage, Long> {

    // ✅ FIX (use entity mapping)
    List<CarImage> findByCarListing_Id(Long carListingId);

    void deleteByCarListing_Id(Long carListingId);
}