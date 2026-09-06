package com.marutixchange.watchlist_service.dto;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WatchlistItemRequest {
    @NotNull private Long carListingId;
    private String carName, carPrice, carImgUrl, carBrand;
    private Integer carYear;
    private String itemType = "WATCHLIST";
}
