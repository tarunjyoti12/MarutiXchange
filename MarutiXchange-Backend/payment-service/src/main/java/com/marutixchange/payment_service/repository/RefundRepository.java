package com.marutixchange.payment_service.repository;

import com.marutixchange.payment_service.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository
        extends JpaRepository<Refund, Long> {

    Optional<Refund> findByTransactionId(
            String transactionId);

    Optional<Refund> findByRefundReference(
            String refundReference);

    List<Refund> findByBuyerId(Long buyerId);

    boolean existsByTransactionId(
            String transactionId);
}