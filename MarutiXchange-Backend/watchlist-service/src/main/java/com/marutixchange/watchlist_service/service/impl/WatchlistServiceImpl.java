package com.marutixchange.watchlist_service.service.impl;
import com.marutixchange.watchlist_service.dto.WatchlistItemRequest;
import com.marutixchange.watchlist_service.dto.WatchlistItemResponse;
import com.marutixchange.watchlist_service.entity.WatchlistItem;
import com.marutixchange.watchlist_service.repository.WatchlistRepository;
import com.marutixchange.watchlist_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor @Slf4j
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;

    @Override @Transactional
    @CacheEvict(value = "watchlist", key = "#userId + '_' + #request.itemType")
    public WatchlistItemResponse addToWatchlist(Long userId, WatchlistItemRequest request) {
        if (watchlistRepository.existsByUserIdAndCarListingIdAndItemType(
                userId, request.getCarListingId(), request.getItemType())) {
            return watchlistRepository
                    .findByUserIdAndCarListingIdAndItemType(userId, request.getCarListingId(), request.getItemType())
                    .map(WatchlistItemResponse::fromEntity)
                    .orElseThrow();
        }
        WatchlistItem item = WatchlistItem.builder()
                .userId(userId).carListingId(request.getCarListingId())
                .carName(request.getCarName()).carPrice(request.getCarPrice())
                .carImgUrl(request.getCarImgUrl()).carBrand(request.getCarBrand())
                .carYear(request.getCarYear()).itemType(request.getItemType())
                .build();
        return WatchlistItemResponse.fromEntity(watchlistRepository.save(item));
    }

    @Override @Transactional
    @CacheEvict(value = "watchlist", key = "#userId + '_' + #itemType")
    public void removeFromWatchlist(Long userId, Long carListingId, String itemType) {
        watchlistRepository.deleteByUserIdAndCarListingIdAndItemType(userId, carListingId, itemType);
        log.info("Removed carListingId={} from {} for userId={}", carListingId, itemType, userId);
    }

    @Override @Transactional(readOnly = true)
    @Cacheable(value = "watchlist", key = "#userId + '_' + #itemType + '_' + #pageable.pageNumber")
    public Page<WatchlistItemResponse> getWatchlist(Long userId, String itemType, Pageable pageable) {
        return watchlistRepository
                .findByUserIdAndItemTypeOrderByAddedAtDesc(userId, itemType, pageable)
                .map(WatchlistItemResponse::fromEntity);
    }

    @Override @Transactional(readOnly = true)
    public boolean isWatched(Long userId, Long carListingId, String itemType) {
        return watchlistRepository.existsByUserIdAndCarListingIdAndItemType(userId, carListingId, itemType);
    }

    @Override @Transactional(readOnly = true)
    public long getWatchlistCount(Long userId, String itemType) {
        return watchlistRepository.countByUserIdAndItemType(userId, itemType);
    }
}
