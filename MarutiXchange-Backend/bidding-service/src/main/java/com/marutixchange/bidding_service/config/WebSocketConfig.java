package com.marutixchange.bidding_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.marutixchange.bidding_service.websocket.BidWebSocketHandler;
import lombok.RequiredArgsConstructor;

/**
 * Registers the raw WebSocket endpoint that the React frontend connects to
 * for real-time outbid and auction-ended notifications.
 *
 * Endpoint: ws://localhost:8086/ws/bids?userId={userId}
 *
 * No STOMP/SockJS — plain WebSocket for simplicity and performance at 30L+ users.
 * For production scale, replace with a Redis Pub/Sub fan-out behind this handler.
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final BidWebSocketHandler bidWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
            .addHandler(bidWebSocketHandler, "/ws/bids")
            .setAllowedOrigins("*"); // Tighten to your domain before production
    }
}
