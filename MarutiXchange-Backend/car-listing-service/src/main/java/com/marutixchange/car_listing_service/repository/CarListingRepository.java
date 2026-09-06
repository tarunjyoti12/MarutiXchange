package com.marutixchange.car_listing_service.repository;

import com.marutixchange.car_listing_service.entity.CarListing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarListingRepository
        extends JpaRepository<CarListing, Long>,
        JpaSpecificationExecutor<CarListing> {

    List<CarListing> findBySellerId(Long sellerId);

    Page<CarListing> findByStatus(
            CarListing.ListingStatus status,
            Pageable pageable);

    Page<CarListing> findByBrandAndStatus(
            String brand,
            CarListing.ListingStatus status,
            Pageable pageable);

    @Query("SELECT c FROM CarListing c WHERE " +
            "c.status = 'ACTIVE' AND " +
            "c.isBoosted = true " +
            "ORDER BY c.createdAt DESC")
    List<CarListing> findFeaturedListings();

    @Query("SELECT c FROM CarListing c WHERE " +
            "c.status = 'ACTIVE' AND " +
            "(:brand IS NULL OR c.brand = :brand) AND " +
            "(:city IS NULL OR c.city = :city) AND " +
            "(:priceFrom IS NULL OR c.askingPrice >= :priceFrom) AND " +
            "(:priceTo IS NULL OR c.askingPrice <= :priceTo)")
    Page<CarListing> searchListings(
            @Param("brand") String brand,
            @Param("city") String city,
            @Param("priceFrom") Double priceFrom,
            @Param("priceTo") Double priceTo,
            Pageable pageable);

    List<CarListing> findTop6ByStatusOrderByViewCountDesc(
            CarListing.ListingStatus status);
}