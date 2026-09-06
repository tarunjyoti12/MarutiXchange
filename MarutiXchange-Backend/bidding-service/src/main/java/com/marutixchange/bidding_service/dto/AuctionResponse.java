package com.marutixchange.bidding_service.dto;

import com.marutixchange.bidding_service.entity.Auction;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuctionResponse {

    private Long id;
    private Long carListingId;
    private Long sellerId;
    private String carName;
    private String carImageUrl;
    private Double startingPrice;
    private Double reservePrice;
    private Double buyNowPrice;
    private Double currentHighestBid;
    private Long currentHighestBidderId;
    private Double minBidIncrement;
    private Integer totalBids;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private Long winnerId;
    private Double winningBid;
    private String city;
    private String fuelType;
    private Integer year;
    private Integer mileage;
    private Long timeRemainingSeconds;
    private Double reserveProgress;
    private LocalDateTime createdAt;

    public static AuctionResponse fromEntity(
            Auction auction) {

        long timeRemaining = 0;
        if (auction.getEndTime() != null
                && auction.getEndTime()
                .isAfter(LocalDateTime.now())) {
            timeRemaining =
                    java.time.Duration.between(
                                    LocalDateTime.now(),
                                    auction.getEndTime())
                            .getSeconds();
        }

        double reserveProgress = 0;
        if (auction.getReservePrice() != null
                && auction.getReservePrice() > 0
                && auction.getCurrentHighestBid() > 0) {
            reserveProgress =
                    (auction.getCurrentHighestBid()
                            / auction.getReservePrice()) * 100;
        }

        return AuctionResponse.builder()
                .id(auction.getId())
                .carListingId(auction.getCarListingId())
                .sellerId(auction.getSellerId())
                .carName(auction.getCarName())
                .carImageUrl(auction.getCarImageUrl())
                .startingPrice(auction.getStartingPrice())
                .reservePrice(auction.getReservePrice())
                .buyNowPrice(auction.getBuyNowPrice())
                .currentHighestBid(
                        auction.getCurrentHighestBid())
                .currentHighestBidderId(
                        auction.getCurrentHighestBidderId())
                .minBidIncrement(
                        auction.getMinBidIncrement())
                .totalBids(auction.getTotalBids())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus().name())
                .winnerId(auction.getWinnerId())
                .winningBid(auction.getWinningBid())
                .city(auction.getCity())
                .fuelType(auction.getFuelType())
                .year(auction.getYear())
                .mileage(auction.getMileage())
                .timeRemainingSeconds(timeRemaining)
                .reserveProgress(
                        Math.min(reserveProgress, 100))
                .createdAt(auction.getCreatedAt())
                .build();
    }
}