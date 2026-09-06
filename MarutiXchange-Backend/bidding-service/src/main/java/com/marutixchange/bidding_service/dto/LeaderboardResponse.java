package com.marutixchange.bidding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Leaderboard payload returned by GET /api/v1/bids/auction/{id}/leaderboard.
 *
 * Designed for the frontend BidLeaderboard component.
 * Bidder names are masked server-side to protect privacy — only the
 * authenticated user's own entry is returned unmasked (identified by position
 * in the ranked list once the frontend compares bidderId to the logged-in user).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardResponse {

    /** Total number of unique bidders in this auction. */
    private int totalBidders;

    /** Ranked entries (1st = highest bid). */
    private List<LeaderboardEntry> entries;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeaderboardEntry {

        /** 1-based rank position. */
        private int rank;

        /** Bidder's user ID — used by the frontend to detect "You". */
        private Long bidderId;

        /**
         * Display name.
         * Server returns masked form (e.g. "R***h") for all bidders.
         * The frontend layer replaces this with "You" when bidderId matches
         * the authenticated user, preserving full name privacy for others.
         */
        private String bidderName;

        /** Bid amount in rupees (not Lakhs — frontend converts). */
        private Double bidAmount;

        /** Whether this entry is the current winning bid. */
        private boolean isWinningBid;

        /** Bid status: ACTIVE, OUTBID, WON, CANCELLED. */
        private String status;
    }
}
