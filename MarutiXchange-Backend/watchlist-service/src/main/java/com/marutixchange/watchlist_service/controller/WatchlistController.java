package com.marutixchange.watchlist_service.controller;
import com.marutixchange.watchlist_service.dto.ApiResponse;
import com.marutixchange.watchlist_service.dto.WatchlistItemRequest;
import com.marutixchange.watchlist_service.dto.WatchlistItemResponse;
import com.marutixchange.watchlist_service.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
@Tag(name = "Watchlist", description = "Save cars to watchlist and compare list")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/users/{userId}")
    @Operation(summary = "Add car to watchlist or compare list")
    public ResponseEntity<ApiResponse<WatchlistItemResponse>> add(
            @PathVariable Long userId,
            @Valid @RequestBody WatchlistItemRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(watchlistService.addToWatchlist(userId, request), "Added to watchlist"));
    }

    @DeleteMapping("/users/{userId}/cars/{carListingId}")
    @Operation(summary = "Remove car from watchlist or compare list")
    public ResponseEntity<ApiResponse<Void>> remove(
            @PathVariable Long userId,
            @PathVariable Long carListingId,
            @RequestParam(defaultValue = "WATCHLIST") String itemType) {
        watchlistService.removeFromWatchlist(userId, carListingId, itemType);
        return ResponseEntity.ok(ApiResponse.success(null, "Removed from watchlist"));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user watchlist or compare list")
    public ResponseEntity<ApiResponse<Page<WatchlistItemResponse>>> getWatchlist(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "WATCHLIST") String itemType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
            ApiResponse.success(
                watchlistService.getWatchlist(userId, itemType, PageRequest.of(page, size)),
                "Watchlist fetched"));
    }

    @GetMapping("/users/{userId}/cars/{carListingId}/status")
    @Operation(summary = "Check if car is in watchlist")
    public ResponseEntity<ApiResponse<Boolean>> isWatched(
            @PathVariable Long userId,
            @PathVariable Long carListingId,
            @RequestParam(defaultValue = "WATCHLIST") String itemType) {
        return ResponseEntity.ok(
            ApiResponse.success(
                watchlistService.isWatched(userId, carListingId, itemType),
                "Status fetched"));
    }

    @GetMapping("/users/{userId}/count")
    @Operation(summary = "Get watchlist count")
    public ResponseEntity<ApiResponse<Long>> count(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "WATCHLIST") String itemType) {
        return ResponseEntity.ok(
            ApiResponse.success(watchlistService.getWatchlistCount(userId, itemType), "Count fetched"));
    }
}
