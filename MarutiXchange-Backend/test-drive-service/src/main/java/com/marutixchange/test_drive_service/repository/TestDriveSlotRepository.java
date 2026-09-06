package com.marutixchange.test_drive_service.repository;

import com.marutixchange.test_drive_service.entity.TestDriveSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TestDriveSlotRepository extends JpaRepository<TestDriveSlot, Long> {

    List<TestDriveSlot> findByListingIdAndSlotDateAndIsActiveTrue(
            Long listingId, LocalDate slotDate);

    /** Available (not booked) slots for a listing on a date */
    List<TestDriveSlot> findByListingIdAndSlotDateAndIsBookedFalseAndIsActiveTrue(
            Long listingId, LocalDate slotDate);

    /** Available slots for a listing across all future dates */
    @Query("SELECT s FROM TestDriveSlot s WHERE s.listingId = :listingId " +
           "AND s.slotDate >= :fromDate AND s.isBooked = false AND s.isActive = true " +
           "ORDER BY s.slotDate ASC, s.slotTime ASC")
    List<TestDriveSlot> findAvailableSlotsFromDate(
            @Param("listingId") Long listingId,
            @Param("fromDate") LocalDate fromDate);

    Optional<TestDriveSlot> findByListingIdAndSlotDateAndSlotTime(
            Long listingId, LocalDate slotDate, LocalTime slotTime);

    List<TestDriveSlot> findBySellerIdAndSlotDate(Long sellerId, LocalDate slotDate);
}
