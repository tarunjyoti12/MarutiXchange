package com.marutixchange.bidding_service.controller;

import com.marutixchange.bidding_service.dto.*;
import com.marutixchange.bidding_service.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /**
     * Place a bid.
     * buyerId is resolved from the JWT token via JwtAuthFilter — never from the request body.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @Valid @RequestBody BidRequest request,
            @RequestAttribute("userId") Long buyerId) {

        BidResponse response = bidService.placeBid(request, buyerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid placed successfully", response));
    }

    /**
     * Buy Now — instant purchase at the buy-now price.
     */
    @PostMapping("/buy-now")
    public ResponseEntity<ApiResponse<BidResponse>> buyNow(
            @Valid @RequestBody BuyNowRequest request,
            @RequestAttribute("userId") Long buyerId) {

        BidResponse response = bidService.buyNow(request, buyerId);
        return ResponseEntity.ok(ApiResponse.success("Purchase successful", response));
    }

    /**
     * Get all bids for an auction, sorted by amount descending.
     * Used by the frontend BidLeaderboard component — refreshed every 10 seconds.
     * Results are cached in Redis for 8 seconds to absorb burst reads at scale (30L+ users).
     *
     * Cache is evicted on every new bid via @CacheEvict in BidServiceImpl.
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getAuctionBids(
            @PathVariable Long auctionId) {

        List<BidResponse> response = bidService.getAuctionBids(auctionId);
        return ResponseEntity.ok(ApiResponse.success("Bids fetched", response));
    }

    /**
     * Get the top-N bids for the leaderboard panel.
     * Separate endpoint so frontend can request a lightweight ranked slice
     * without pulling the full bid history.
     *
     * @param auctionId auction to query
     * @param limit     max entries to return (default 10, max 50)
     */
    @GetMapping("/auction/{auctionId}/leaderboard")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getLeaderboard(
            @PathVariable Long auctionId,
            @RequestParam(defaultValue = "10") int limit) {

        if (limit > 50) limit = 50;
        LeaderboardResponse response = bidService.getLeaderboard(auctionId, limit);
        return ResponseEntity.ok(ApiResponse.success("Leaderboard fetched", response));
    }

    /**
     * Get the authenticated user's own bids.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids(
            @RequestAttribute("userId") Long buyerId) {

        List<BidResponse> response = bidService.getUserBids(buyerId);
        return ResponseEntity.ok(ApiResponse.success("User bids fetched", response));
    }
}
