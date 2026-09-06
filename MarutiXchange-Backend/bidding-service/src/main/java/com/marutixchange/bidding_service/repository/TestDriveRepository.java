package com.marutixchange.bidding_service.repository;

import com.marutixchange.bidding_service.entity.TestDrive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {

    List<TestDrive> findByBuyerId(Long buyerId);

    List<TestDrive> findBySellerId(Long sellerId);

    List<TestDrive> findByCarListingId(Long carListingId);
}