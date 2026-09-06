package com.marutixchange.payment_service.repository;

import com.marutixchange.payment_service.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByTransactionId(
            String transactionId);

    Optional<Payment> findByIdempotencyKey(
            String idempotencyKey);

    List<Payment> findByBuyerId(Long buyerId);

    List<Payment> findBySellerId(Long sellerId);

    List<Payment> findByCarListingId(
            Long carListingId);

    List<Payment> findByAuctionId(Long auctionId);

    List<Payment> findByBuyerIdAndStatus(
            Long buyerId, Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE " +
            "p.status = 'PENDING' AND " +
            "p.timeoutAt <= :now")
    List<Payment> findTimedOutPayments(
            LocalDateTime now);

    @Query("SELECT COUNT(p) FROM Payment p WHERE " +
            "p.buyerId = :buyerId AND " +
            "p.status = 'FAILED' AND " +
            "p.createdAt >= :since")
    long countFailedAttempts(
            Long buyerId, LocalDateTime since);
}