package com.marutixchange.bidding_service.dto;

import com.marutixchange.bidding_service.entity.Bid;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BidResponse {

    private Long id;
    private Long auctionId;
    private Long bidderId;
    private String bidderName;
    private Double bidAmount;
    private String status;
    private Boolean isWinningBid;
    private Boolean isBuyNow;
    private LocalDateTime placedAt;

    public static BidResponse fromEntity(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .auctionId(bid.getAuction().getId())
                .bidderId(bid.getBidderId())
                .bidderName(bid.getBidderName())
                .bidAmount(bid.getBidAmount())
                .status(bid.getStatus().name())
                .isWinningBid(bid.getIsWinningBid())
                .isBuyNow(bid.getIsBuyNow())
                .placedAt(bid.getPlacedAt())
                .build();
    }
}