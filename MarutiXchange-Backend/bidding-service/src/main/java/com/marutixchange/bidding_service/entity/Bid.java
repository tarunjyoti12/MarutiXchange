package com.marutixchange.bidding_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "bids",
        indexes = {
                @Index(name = "idx_bid_auction", columnList = "auction_id"),
                @Index(name = "idx_bid_bidder", columnList = "bidder_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // ✅ optimized
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @Column(name = "bidder_id", nullable = false)
    private Long bidderId;

    @Column(name = "bidder_name")
    private String bidderName;

    @Column(name = "bid_amount", nullable = false)
    private Double bidAmount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BidStatus status = BidStatus.ACTIVE;

    @Column(name = "is_winning_bid", nullable = false)
    @Builder.Default
    private Boolean isWinningBid = false;

    @Column(name = "is_buy_now", nullable = false)
    @Builder.Default
    private Boolean isBuyNow = false;

    @CreationTimestamp
    @Column(name = "placed_at", updatable = false)
    private LocalDateTime placedAt;

    public enum BidStatus {
        ACTIVE,
        OUTBID,
        WON,
        CANCELLED
    }
}