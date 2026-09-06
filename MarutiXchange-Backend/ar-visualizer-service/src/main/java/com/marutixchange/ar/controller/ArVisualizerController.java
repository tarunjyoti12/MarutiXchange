package com.marutixchange.ar.controller;

import com.marutixchange.ar.dto.ArSessionRequest;
import com.marutixchange.ar.dto.ArSessionResponse;
import com.marutixchange.ar.service.ArVisualizerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ar")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "${app.frontend.url}"})
public class ArVisualizerController {

    private final ArVisualizerService arVisualizerService;

    /**
     * Start an AR session for a car listing
     * POST /api/v1/ar/session/start
     */
    @PostMapping("/session/start")
    public ResponseEntity<ArSessionResponse> startSession(
            @Valid @RequestBody ArSessionRequest request) {
        log.info("AR session start request for carId: {}, userId: {}",
                request.getCarId(), request.getUserId());
        ArSessionResponse response = arVisualizerService.startSession(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Save AR session screenshot
     * POST /api/v1/ar/session/{sessionId}/screenshot
     */
    @PostMapping("/session/{sessionId}/screenshot")
    public ResponseEntity<Void> saveScreenshot(
            @PathVariable String sessionId,
            @RequestBody String screenshotBase64) {
        arVisualizerService.saveScreenshot(sessionId, screenshotBase64);
        return ResponseEntity.ok().build();
    }

    /**
     * Get AR analytics for a car listing
     * GET /api/v1/ar/analytics/{carId}
     */
    @GetMapping("/analytics/{carId}")
    public ResponseEntity<?> getAnalytics(@PathVariable Long carId) {
        return ResponseEntity.ok(arVisualizerService.getAnalytics(carId));
    }

    /**
     * Log AR event (view, color change, resize)
     * POST /api/v1/ar/event
     */
    @PostMapping("/event")
    public ResponseEntity<Void> logEvent(@RequestBody ArEventRequest event) {
        arVisualizerService.logEvent(event);
        return ResponseEntity.ok().build();
    }
}
