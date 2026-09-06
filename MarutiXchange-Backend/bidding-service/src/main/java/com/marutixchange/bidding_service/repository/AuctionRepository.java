package com.marutixchange.bidding_service.repository;

import com.marutixchange.bidding_service.entity.Auction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findByStatus(Auction.AuctionStatus status);

    List<Auction> findBySellerId(Long sellerId);

    List<Auction> findByCarListingId(Long carListingId);

    // 🔥 LOCK for bidding (VERY IMPORTANT)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Auction a WHERE a.id = :id")
    Optional<Auction> findByIdForUpdate(@Param("id") Long id);

    // Live auctions
    @Query("SELECT a FROM Auction a WHERE a.status = 'LIVE' ORDER BY a.endTime ASC")
    List<Auction> findLiveAuctions();

    // Scheduler queries
    @Query("SELECT a FROM Auction a WHERE a.status = 'SCHEDULED' AND a.startTime <= :now")
    List<Auction> findAuctionsToStart(@Param("now") LocalDateTime now);

    @Query("SELECT a FROM Auction a WHERE a.status = 'LIVE' AND a.endTime <= :now")
    List<Auction> findAuctionsToEnd(@Param("now") LocalDateTime now);
}