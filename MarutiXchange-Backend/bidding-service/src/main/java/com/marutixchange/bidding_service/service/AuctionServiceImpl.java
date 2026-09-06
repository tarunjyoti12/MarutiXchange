package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.AuctionRequest;
import com.marutixchange.bidding_service.dto.AuctionResponse;
import com.marutixchange.bidding_service.entity.Auction;
import com.marutixchange.bidding_service.exception.AuctionNotFoundException;
import com.marutixchange.bidding_service.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;

    @Override
    @Transactional
    public AuctionResponse createAuction(AuctionRequest request, Long sellerId) {

        Auction auction = Auction.builder()
                .carListingId(request.getCarListingId())
                .sellerId(sellerId)
                .carName(request.getCarName())
                .carImageUrl(request.getCarImageUrl())
                .startingPrice(request.getStartingPrice())
                .reservePrice(request.getReservePrice())
                .buyNowPrice(request.getBuyNowPrice())
                .currentHighestBid(0.0)
                .minBidIncrement(
                        request.getMinBidIncrement() != null
                                ? request.getMinBidIncrement() : 1000.0)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .city(request.getCity())
                .fuelType(request.getFuelType())
                .year(request.getYear())
                .mileage(request.getMileage())
                .status(Auction.AuctionStatus.SCHEDULED)
                .build();

        if (!auction.getStartTime().isAfter(LocalDateTime.now())) {
            auction.setStatus(Auction.AuctionStatus.LIVE);
        }

        return AuctionResponse.fromEntity(auctionRepository.save(auction));
    }

    @Override
    public AuctionResponse getAuctionById(Long id) {
        return AuctionResponse.fromEntity(
                auctionRepository.findById(id)
                        .orElseThrow(() -> new AuctionNotFoundException(id))
        );
    }

    @Override
    public List<AuctionResponse> getLiveAuctions() {
        return auctionRepository.findLiveAuctions()
                .stream().map(AuctionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAll()
                .stream().map(AuctionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    public List<AuctionResponse> getSellerAuctions(Long sellerId) {
        return auctionRepository.findBySellerId(sellerId)
                .stream().map(AuctionResponse::fromEntity).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AuctionResponse endAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));

        auction.setStatus(Auction.AuctionStatus.ENDED);
        return AuctionResponse.fromEntity(auctionRepository.save(auction));
    }

    @Override
    @Transactional
    public AuctionResponse cancelAuction(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new AuctionNotFoundException(id));

        auction.setStatus(Auction.AuctionStatus.CANCELLED);
        return AuctionResponse.fromEntity(auctionRepository.save(auction));
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndUpdateAuctionStatuses() {

        LocalDateTime now = LocalDateTime.now();

        auctionRepository.findAuctionsToStart(now).forEach(a -> {
            a.setStatus(Auction.AuctionStatus.LIVE);
            auctionRepository.save(a);
        });

        auctionRepository.findAuctionsToEnd(now).forEach(a -> endAuction(a.getId()));
    }
}