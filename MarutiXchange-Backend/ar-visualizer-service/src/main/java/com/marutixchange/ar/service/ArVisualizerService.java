package com.marutixchange.ar.service;

import com.marutixchange.ar.controller.ArEventRequest;
import com.marutixchange.ar.dto.ArSessionRequest;
import com.marutixchange.ar.dto.ArSessionResponse;
import com.marutixchange.ar.model.ArSession;
import com.marutixchange.ar.repository.ArSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ArVisualizerService {

    private final ArSessionRepository arSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AR_SESSION_KEY  = "ar:session:";
    private static final String AR_ANALYTICS_KEY = "ar:analytics:";

    // ── Start AR Session ──────────────────────────────────────────────────────
    @Transactional
    public ArSessionResponse startSession(ArSessionRequest request) {
        String sessionId = UUID.randomUUID().toString();

        // Save session to PostgreSQL
        ArSession session = ArSession.builder()
                .sessionId(sessionId)
                .carId(request.getCarId())
                .userId(request.getUserId())
                .carName(request.getCarName())
                .deviceType(request.getDeviceType())
                .colorChanges(0)
                .resizeCount(0)
                .screenshotTaken(false)
                .status(ArSession.ArSessionStatus.ACTIVE)
                .build();

        arSessionRepository.save(session);

        // Cache in Redis for fast lookup (TTL: 30 mins)
        redisTemplate.opsForValue().set(
            AR_SESSION_KEY + sessionId,
            session,
            Duration.ofMinutes(30)
        );

        // Increment AR view count in Redis
        redisTemplate.opsForValue().increment("ar:views:car:" + request.getCarId());

        log.info("AR session started: {} for carId: {}", sessionId, request.getCarId());

        return ArSessionResponse.builder()
                .sessionId(sessionId)
                .carId(request.getCarId())
                .carName(request.getCarName())
                .status("ACTIVE")
                .message("AR session started successfully")
                .build();
    }

    // ── Save Screenshot ───────────────────────────────────────────────────────
    @Transactional
    public void saveScreenshot(String sessionId, String screenshotBase64) {
        arSessionRepository.findBySessionId(sessionId).ifPresent(session -> {
            session.setScreenshotTaken(true);
            arSessionRepository.save(session);
            log.info("Screenshot saved for session: {}", sessionId);
        });
    }

    // ── Get Analytics (cached in Redis) ──────────────────────────────────────
    @Cacheable(value = "ar-analytics", key = "#carId")
    public Map<String, Object> getAnalytics(Long carId) {
        long totalSessions    = arSessionRepository.countByCarId(carId);
        long completedSessions= arSessionRepository.countCompletedSessionsByCarId(carId);
        Double avgDuration    = arSessionRepository.avgDurationByCarId(carId);
        long screenshots      = arSessionRepository.countScreenshotsByCarId(carId);

        // Get real-time view count from Redis
        Object redisViews = redisTemplate.opsForValue().get("ar:views:car:" + carId);
        long totalViews = redisViews != null ? Long.parseLong(redisViews.toString()) : totalSessions;

        return Map.of(
            "carId",             carId,
            "totalArViews",      totalViews,
            "totalSessions",     totalSessions,
            "completedSessions", completedSessions,
            "avgDurationSeconds",avgDuration != null ? avgDuration.longValue() : 0,
            "screenshotsTaken",  screenshots,
            "engagementRate",    totalSessions > 0
                ? String.format("%.1f%%", (completedSessions * 100.0 / totalSessions))
                : "0%"
        );
    }

    // ── Log AR Event ──────────────────────────────────────────────────────────
    @Transactional
    public void logEvent(ArEventRequest event) {
        arSessionRepository.findBySessionId(event.getSessionId()).ifPresent(session -> {
            switch (event.getEventType()) {
                case "COLOR_CHANGE" -> session.setColorChanges(
                    (session.getColorChanges() == null ? 0 : session.getColorChanges()) + 1);
                case "RESIZE" -> session.setResizeCount(
                    (session.getResizeCount() == null ? 0 : session.getResizeCount()) + 1);
                case "COMPLETE" -> session.setStatus(ArSession.ArSessionStatus.COMPLETED);
                case "ABANDON"  -> session.setStatus(ArSession.ArSessionStatus.ABANDONED);
            }
            arSessionRepository.save(session);

            // Invalidate analytics cache
            redisTemplate.delete(AR_ANALYTICS_KEY + session.getCarId());
        });
    }
}
