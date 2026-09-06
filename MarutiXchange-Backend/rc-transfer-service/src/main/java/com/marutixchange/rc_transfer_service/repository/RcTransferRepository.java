package com.marutixchange.rc_transfer_service.repository;

import com.marutixchange.rc_transfer_service.entity.RcTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RcTransferRepository extends JpaRepository<RcTransfer, Long> {

    Optional<RcTransfer> findByOrderId(Long orderId);

    Page<RcTransfer> findByBuyerIdOrderByCreatedAtDesc(Long buyerId, Pageable pageable);
    Page<RcTransfer> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);

    List<RcTransfer> findByStatus(RcTransfer.TransferStatus status);

    @Query("SELECT r FROM RcTransfer r WHERE r.buyerId = :userId OR r.sellerId = :userId ORDER BY r.createdAt DESC")
    Page<RcTransfer> findAllByUserId(Long userId, Pageable pageable);

    long countByBuyerIdAndStatusNotIn(Long buyerId, List<RcTransfer.TransferStatus> statuses);
}
