package com.marutixchange.payment_service.repository;

import com.marutixchange.payment_service.entity.Escrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EscrowRepository
        extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByEscrowReference(
            String escrowReference);

    Optional<Escrow> findByPaymentId(Long paymentId);

    List<Escrow> findByBuyerId(Long buyerId);

    List<Escrow> findBySellerId(Long sellerId);

    List<Escrow> findByCarListingId(
            Long carListingId);

    @Query("SELECT e FROM Escrow e WHERE " +
            "e.status = 'DELIVERY_ACTIVE' AND " +
            "e.autoReleaseAt <= :now")
    List<Escrow> findEscrowsForAutoRelease(
            LocalDateTime now);
}