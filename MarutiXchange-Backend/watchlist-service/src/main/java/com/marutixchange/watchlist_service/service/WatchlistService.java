package com.marutixchange.watchlist_service.service;
import com.marutixchange.watchlist_service.dto.WatchlistItemRequest;
import com.marutixchange.watchlist_service.dto.WatchlistItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WatchlistService {
    WatchlistItemResponse addToWatchlist(Long userId, WatchlistItemRequest request);
    void removeFromWatchlist(Long userId, Long carListingId, String itemType);
    Page<WatchlistItemResponse> getWatchlist(Long userId, String itemType, Pageable pageable);
    boolean isWatched(Long userId, Long carListingId, String itemType);
    long getWatchlistCount(Long userId, String itemType);
}
