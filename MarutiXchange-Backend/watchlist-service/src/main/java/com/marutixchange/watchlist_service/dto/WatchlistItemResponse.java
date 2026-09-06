package com.marutixchange.watchlist_service.dto;
import com.marutixchange.watchlist_service.entity.WatchlistItem;
import lombok.*;
import java.time.LocalDateTime;

@Data @Builder
public class WatchlistItemResponse {
    private Long id, userId, carListingId;
    private String carName, carPrice, carImgUrl, carBrand, itemType;
    private Integer carYear;
    private LocalDateTime addedAt;

    public static WatchlistItemResponse fromEntity(WatchlistItem e) {
        return WatchlistItemResponse.builder()
                .id(e.getId()).userId(e.getUserId()).carListingId(e.getCarListingId())
                .carName(e.getCarName()).carPrice(e.getCarPrice()).carImgUrl(e.getCarImgUrl())
                .carBrand(e.getCarBrand()).carYear(e.getCarYear())
                .itemType(e.getItemType()).addedAt(e.getAddedAt()).build();
    }
}
