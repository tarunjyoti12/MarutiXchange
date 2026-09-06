package com.marutixchange.test_drive_service.repository;

import com.marutixchange.test_drive_service.entity.TestDrive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TestDriveRepository extends JpaRepository<TestDrive, Long> {

    Page<TestDrive> findByBuyerId(Long buyerId, Pageable pageable);

    Page<TestDrive> findBySellerId(Long sellerId, Pageable pageable);

    List<TestDrive> findByListingId(Long listingId);

    Page<TestDrive> findByStatus(TestDrive.TestDriveStatus status, Pageable pageable);

    /** Count active bookings for a buyer (PENDING or CONFIRMED) */
    @Query("SELECT COUNT(t) FROM TestDrive t WHERE t.buyerId = :buyerId " +
           "AND t.status IN ('PENDING', 'CONFIRMED')")
    long countActiveBookingsByBuyer(@Param("buyerId") Long buyerId);

    /** Check if the same slot is already taken for this listing */
    @Query("SELECT COUNT(t) > 0 FROM TestDrive t WHERE " +
           "t.listingId = :listingId AND " +
           "t.slotDate = :slotDate AND " +
           "t.slotTime = :slotTime AND " +
           "t.status IN ('PENDING', 'CONFIRMED')")
    boolean existsConflictingBooking(
            @Param("listingId") Long listingId,
            @Param("slotDate") LocalDate slotDate,
            @Param("slotTime") java.time.LocalTime slotTime);

    List<TestDrive> findByBuyerIdAndStatus(Long buyerId, TestDrive.TestDriveStatus status);

    List<TestDrive> findBySellerIdAndStatus(Long sellerId, TestDrive.TestDriveStatus status);
}
