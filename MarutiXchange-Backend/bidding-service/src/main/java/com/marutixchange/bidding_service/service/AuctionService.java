package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.AuctionRequest;
import com.marutixchange.bidding_service.dto.AuctionResponse;

import java.util.List;

public interface AuctionService {

    AuctionResponse createAuction(AuctionRequest request, Long sellerId);

    AuctionResponse getAuctionById(Long id);

    List<AuctionResponse> getLiveAuctions();

    List<AuctionResponse> getAllAuctions();

    List<AuctionResponse> getSellerAuctions(Long sellerId);

    AuctionResponse endAuction(Long id);

    AuctionResponse cancelAuction(Long id);

    void checkAndUpdateAuctionStatuses();
}