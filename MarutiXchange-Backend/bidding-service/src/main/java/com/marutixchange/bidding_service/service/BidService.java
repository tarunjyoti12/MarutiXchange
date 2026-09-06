package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.BidRequest;
import com.marutixchange.bidding_service.dto.BidResponse;
import com.marutixchange.bidding_service.dto.BuyNowRequest;
import com.marutixchange.bidding_service.dto.LeaderboardResponse;

import java.util.List;

public interface BidService {

    BidResponse placeBid(BidRequest request, Long buyerId);

    BidResponse buyNow(BuyNowRequest request, Long buyerId);

    List<BidResponse> getAuctionBids(Long auctionId);

    List<BidResponse> getUserBids(Long userId);

    LeaderboardResponse getLeaderboard(Long auctionId, int limit);
}