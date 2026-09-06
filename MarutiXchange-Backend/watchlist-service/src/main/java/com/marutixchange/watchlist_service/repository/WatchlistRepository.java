package com.marutixchange.watchlist_service.repository;
import com.marutixchange.watchlist_service.entity.WatchlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    Page<WatchlistItem> findByUserIdAndItemTypeOrderByAddedAtDesc(Long userId, String itemType, Pageable pageable);
    Optional<WatchlistItem> findByUserIdAndCarListingIdAndItemType(Long userId, Long carListingId, String itemType);
    boolean existsByUserIdAndCarListingIdAndItemType(Long userId, Long carListingId, String itemType);
    long countByUserIdAndItemType(Long userId, String itemType);
    void deleteByUserIdAndCarListingIdAndItemType(Long userId, Long carListingId, String itemType);
}
