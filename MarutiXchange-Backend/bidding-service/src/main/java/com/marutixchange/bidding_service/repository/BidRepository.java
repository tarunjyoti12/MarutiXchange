package com.marutixchange.bidding_service.repository;

import com.marutixchange.bidding_service.entity.Bid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    // All bids for an auction, highest first — used by leaderboard & full bid history
    List<Bid> findByAuction_IdOrderByBidAmountDesc(Long auctionId);

    // Top-N bids for the leaderboard endpoint (Pageable limits result count)
    List<Bid> findByAuction_IdOrderByBidAmountDesc(Long auctionId, Pageable pageable);

    List<Bid> findByBidderId(Long bidderId);

    List<Bid> findByAuction_IdAndBidderId(Long auctionId, Long bidderId);

    // Highest single bid
    Optional<Bid> findTopByAuction_IdOrderByBidAmountDesc(Long auctionId);

    // Count unique bidders for a given auction
    @Query("SELECT COUNT(DISTINCT b.bidderId) FROM Bid b WHERE b.auction.id = :auctionId")
    int countUniqueBidders(@Param("auctionId") Long auctionId);

    // Mark previous winning bids as OUTBID before inserting a new winner
    @Modifying
    @Query("UPDATE Bid b SET b.status = 'OUTBID', b.isWinningBid = false " +
           "WHERE b.auction.id = :auctionId AND b.isWinningBid = true")
    void markPreviousBidsOutbid(@Param("auctionId") Long auctionId);
}
