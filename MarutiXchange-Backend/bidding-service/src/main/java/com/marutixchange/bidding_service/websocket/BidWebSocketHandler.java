package com.marutixchange.bidding_service.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active WebSocket connections from the React frontend.
 *
 * Each connected user registers their session here keyed by userId.
 * When BidServiceImpl places a bid, it calls BidWebSocketHandler.notifyOutbid()
 * to push a real-time alert to the displaced highest bidder.
 *
 * Message format sent to frontend:
 * {
 *   "type":      "OUTBID" | "AUCTION_ENDED" | "BID_PLACED",
 *   "auctionId": 123,
 *   "bidderId":  456,      // the user who was outbid
 *   "bidAmount": 750000    // the new highest bid in rupees
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BidWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;

    // userId → WebSocketSession (concurrent — multiple tabs per user allowed)
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket connected: userId={}, sessionId={}", userId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = extractUserId(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Frontend sends ping to keep connection alive — respond with pong
        if ("ping".equals(message.getPayload())) {
            try { session.sendMessage(new TextMessage("pong")); } catch (IOException ignored) {}
        }
    }

    // ── Called by BidServiceImpl after a new bid displaces the previous winner ──

    public void notifyOutbid(Long outbidUserId, Long auctionId, Double newBidAmount) {
        WebSocketSession session = sessions.get(outbidUserId);
        if (session == null || !session.isOpen()) return;

        try {
            Map<String, Object> payload = Map.of(
                "type",      "OUTBID",
                "auctionId", auctionId,
                "bidderId",  outbidUserId,
                "bidAmount", newBidAmount
            );
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            log.info("Outbid notification sent to userId={} for auctionId={}", outbidUserId, auctionId);
        } catch (IOException e) {
            log.error("Failed to send outbid notification to userId={}: {}", outbidUserId, e.getMessage());
        }
    }

    public void notifyAuctionEnded(Long auctionId, Long winnerId, Double winningBid) {
        sessions.forEach((userId, session) -> {
            if (!session.isOpen()) return;
            try {
                String type = userId.equals(winnerId) ? "AUCTION_WON" : "AUCTION_ENDED";
                Map<String, Object> payload = Map.of(
                    "type",       type,
                    "auctionId",  auctionId,
                    "winnerId",   winnerId,
                    "winningBid", winningBid
                );
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
            } catch (IOException e) {
                log.error("Failed to send auction-ended to userId={}: {}", userId, e.getMessage());
            }
        });
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Long extractUserId(WebSocketSession session) {
        String query = session.getUri() != null ? session.getUri().getQuery() : null;
        if (query == null) return null;
        for (String param : query.split("&")) {
            String[] kv = param.split("=");
            if (kv.length == 2 && "userId".equals(kv[0])) {
                try { return Long.parseLong(kv[1]); } catch (NumberFormatException ignored) {}
            }
        }
        return null;
    }
}
