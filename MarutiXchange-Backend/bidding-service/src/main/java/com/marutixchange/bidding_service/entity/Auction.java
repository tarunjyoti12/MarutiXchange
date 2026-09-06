package com.marutixchange.bidding_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "auctions",
        indexes = {
                @Index(name = "idx_auction_car", columnList = "car_listing_id"),
                @Index(name = "idx_auction_seller", columnList = "seller_id"),
                @Index(name = "idx_auction_status", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version = 0L;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "car_name", nullable = false)
    private String carName;

    @Column(name = "car_image_url")
    private String carImageUrl;

    @Column(name = "starting_price", nullable = false)
    private Double startingPrice;

    @Column(name = "reserve_price")
    private Double reservePrice;

    @Column(name = "buy_now_price")
    private Double buyNowPrice;

    @Column(name = "current_highest_bid", nullable = false)
    @Builder.Default
    private Double currentHighestBid = 0.0;

    @Column(name = "current_highest_bidder_id")
    private Long currentHighestBidderId;

    @Column(name = "min_bid_increment", nullable = false)
    @Builder.Default
    private Double minBidIncrement = 1000.0;

    @Column(name = "total_bids", nullable = false)
    @Builder.Default
    private Integer totalBids = 0;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.SCHEDULED;

    @Column(name = "winner_id")
    private Long winnerId;

    @Column(name = "winning_bid")
    private Double winningBid;

    @Column(name = "city")
    private String city;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "year")
    private Integer year;

    @Column(name = "mileage")
    private Integer mileage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "auction",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    private List<Bid> bids;

    public enum AuctionStatus {
        SCHEDULED,
        LIVE,
        ENDED,
        CANCELLED,
        BUY_NOW_SOLD
    }
}