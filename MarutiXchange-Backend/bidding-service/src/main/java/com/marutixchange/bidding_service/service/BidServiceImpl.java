package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.client.RuleEngineClient;
import com.marutixchange.bidding_service.dto.*;
import com.marutixchange.bidding_service.entity.Auction;
import com.marutixchange.bidding_service.entity.Bid;
import com.marutixchange.bidding_service.exception.*;
import com.marutixchange.bidding_service.repository.AuctionRepository;
import com.marutixchange.bidding_service.repository.BidRepository;
import com.marutixchange.bidding_service.websocket.BidWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidServiceImpl implements BidService {

    private final BidRepository       bidRepository;
    private final AuctionRepository   auctionRepository;
    private final RuleEngineClient    ruleEngineClient;
    private final RestTemplate        restTemplate;
    private final BidWebSocketHandler bidWebSocketHandler;

    @Value("${notification.service.url}")
    private String notificationUrl;

    @Value("${app.services.order-url:http://localhost:8066}")
    private String orderServiceUrl;

    // ─── Place Bid ────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BidResponse placeBid(BidRequest request, Long buyerId) {

        Auction auction = auctionRepository.findByIdForUpdate(request.getAuctionId())
                .orElseThrow(() -> new AuctionNotFoundException(request.getAuctionId()));

        // ── Rules Engine validation (non-blocking — falls back if service is down) ──
        boolean rulesApproved = true;
        String  rulesMessage  = "Approved";

        try {
            Map<String, Object> ruleRequest = new HashMap<>();
            ruleRequest.put("type",             "BID");
            ruleRequest.put("auctionId",        request.getAuctionId());
            ruleRequest.put("buyerId",          buyerId);
            ruleRequest.put("bidAmount",        request.getBidAmount());
            ruleRequest.put("currentHighestBid", auction.getCurrentHighestBid());

            log.info("Calling Rule Engine: {}", ruleRequest);
            Map<String, Object> result = ruleEngineClient.evaluateRules(ruleRequest);
            log.info("Rule Engine response: {}", result);

            rulesApproved = (Boolean) result.getOrDefault("approved", true);
            rulesMessage  = (String)  result.getOrDefault("message",  "Approved");

        } catch (Exception e) {
            // Rules Engine not running — apply basic local validation instead
            log.warn("Rules Engine unavailable: {} — applying local validation", e.getMessage());

            if (auction.getCurrentHighestBid() != null &&
                    request.getBidAmount() <= auction.getCurrentHighestBid()) {
                throw new BidTooLowException(
                    "Bid must be higher than current bid of Rs." + auction.getCurrentHighestBid());
            }
            // Local validation passed — allow bid through
            rulesApproved = true;
        }

        if (!rulesApproved) {
            throw new BidTooLowException(rulesMessage);
        }

        // ── Check auction is still live ───────────────────────────────────────
        if (auction.getStatus() != Auction.AuctionStatus.LIVE) {
            throw new AuctionEndedException("Auction is not live");
        }

        // Capture previous highest bidder BEFORE marking as outbid
        Long previousHighestBidderId = auction.getCurrentHighestBidderId();

        bidRepository.markPreviousBidsOutbid(auction.getId());

        Bid bid = Bid.builder()
                .auction(auction)
                .bidderId(buyerId)
                .bidderName(request.getBidderName())
                .bidAmount(request.getBidAmount())
                .isWinningBid(true)
                .status(Bid.BidStatus.ACTIVE)
                .build();

        bidRepository.save(bid);

        auction.setCurrentHighestBid(request.getBidAmount());
        auction.setCurrentHighestBidderId(buyerId);
        auction.setTotalBids(auction.getTotalBids() + 1);
        auctionRepository.save(auction);

        // ── Real-time WebSocket outbid alert ──────────────────────────────────
        if (previousHighestBidderId != null &&
                !previousHighestBidderId.equals(buyerId)) {
            try {
                bidWebSocketHandler.notifyOutbid(
                        previousHighestBidderId,
                        auction.getId(),
                        request.getBidAmount()
                );
                log.info("Outbid WebSocket sent to userId={}", previousHighestBidderId);
            } catch (Exception e) {
                log.warn("WebSocket notification failed: {}", e.getMessage());
            }
        }

        // ── Kafka notification ────────────────────────────────────────────────
        sendNotification(buyerId, "BID_PLACED",
                "Bid Placed",
                "Your bid of Rs." + request.getBidAmount() + " was placed successfully.");

        return BidResponse.fromEntity(bid);
    }

    // ─── Buy Now ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public BidResponse buyNow(BuyNowRequest request, Long buyerId) {

        Auction auction = auctionRepository.findByIdForUpdate(request.getAuctionId())
                .orElseThrow(() -> new AuctionNotFoundException(request.getAuctionId()));

        if (auction.getStatus() != Auction.AuctionStatus.LIVE) {
            throw new AuctionEndedException("Auction is not live");
        }

        if (auction.getBuyNowPrice() == null) {
            throw new BidTooLowException("Buy Now not available for this auction");
        }

        bidRepository.markPreviousBidsOutbid(auction.getId());

        Bid bid = Bid.builder()
                .auction(auction)
                .bidderId(buyerId)
                .bidderName(request.getBuyerName())
                .bidAmount(auction.getBuyNowPrice())
                .isWinningBid(true)
                .isBuyNow(true)
                .status(Bid.BidStatus.WON)
                .build();

        bidRepository.save(bid);

        auction.setStatus(Auction.AuctionStatus.BUY_NOW_SOLD);
        auction.setWinnerId(buyerId);
        auction.setWinningBid(auction.getBuyNowPrice());
        auction.setCurrentHighestBid(auction.getBuyNowPrice());
        auction.setCurrentHighestBidderId(buyerId);
        auctionRepository.save(auction);

        // Notify all connected users that auction ended via Buy Now
        try {
            bidWebSocketHandler.notifyAuctionEnded(
                    auction.getId(),
                    buyerId,
                    auction.getBuyNowPrice()
            );
        } catch (Exception e) {
            log.warn("WebSocket auction-ended notification failed: {}", e.getMessage());
        }

        // Auto-create order
        try {
            Map<String, Object> orderRequest = new HashMap<>();
            orderRequest.put("carListingId",  auction.getCarListingId());
            orderRequest.put("buyerId",       buyerId);
            orderRequest.put("sellerId",      auction.getSellerId());
            orderRequest.put("amount",        auction.getBuyNowPrice());
            orderRequest.put("paymentMethod", "UPI");

            Map orderResponse = restTemplate.postForObject(
                    orderServiceUrl + "/api/orders", orderRequest, Map.class);
            log.info("Order auto-created after Buy Now: {}", orderResponse);
        } catch (Exception e) {
            log.error("Failed to auto-create order after buy-now: {}", e.getMessage());
        }

        sendNotification(buyerId, "BUY_NOW_SUCCESS",
                "Purchase Successful",
                "You bought the car for Rs." + auction.getBuyNowPrice() + ". Complete your payment to confirm.");

        sendNotification(auction.getSellerId(), "BUY_NOW_SUCCESS",
                "Your Car Was Purchased",
                "A buyer purchased your car via Buy Now for Rs." + auction.getBuyNowPrice() + ".");

        return BidResponse.fromEntity(bid);
    }

    // ─── Get All Bids for Auction ─────────────────────────────────────────────

    @Override
    public List<BidResponse> getAuctionBids(Long auctionId) {
        return bidRepository.findByAuction_IdOrderByBidAmountDesc(auctionId)
                .stream().map(BidResponse::fromEntity).collect(Collectors.toList());
    }

    // ─── Leaderboard ─────────────────────────────────────────────────────────

    @Override
    public LeaderboardResponse getLeaderboard(Long auctionId, int limit) {
        List<Bid> topBids = bidRepository.findByAuction_IdOrderByBidAmountDesc(
                auctionId, PageRequest.of(0, limit));

        int totalBidders = bidRepository.countUniqueBidders(auctionId);

        List<LeaderboardResponse.LeaderboardEntry> entries = IntStream
                .range(0, topBids.size())
                .mapToObj(i -> {
                    Bid b = topBids.get(i);
                    return LeaderboardResponse.LeaderboardEntry.builder()
                            .rank(i + 1)
                            .bidderId(b.getBidderId())
                            .bidderName(maskName(b.getBidderName()))
                            .bidAmount(b.getBidAmount())
                            .isWinningBid(b.getIsWinningBid())
                            .status(b.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());

        return LeaderboardResponse.builder()
                .totalBidders(totalBidders)
                .entries(entries)
                .build();
    }

    // ─── User's Own Bids ──────────────────────────────────────────────────────

    @Override
    public List<BidResponse> getUserBids(Long userId) {
        return bidRepository.findByBidderId(userId)
                .stream().map(BidResponse::fromEntity).collect(Collectors.toList());
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "Bidder";
        String first = name.trim().split(" ")[0];
        if (first.length() <= 2) return first.charAt(0) + "**";
        return first.charAt(0)
                + "*".repeat(first.length() - 2)
                + first.charAt(first.length() - 1);
    }

    private void sendNotification(Long userId, String type, String title, String body) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("userId",           userId);
            notification.put("title",            title);
            notification.put("body",             body);
            notification.put("notificationType", type);
            notification.put("channel",          "IN_APP");

            restTemplate.postForObject(notificationUrl, notification, String.class);
            log.info("Notification sent to userId={}", userId);
        } catch (Exception e) {
            log.error("Notification failed for userId={}: {}", userId, e.getMessage());
        }
    }
}