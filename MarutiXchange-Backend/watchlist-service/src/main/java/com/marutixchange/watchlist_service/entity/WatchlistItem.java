package com.marutixchange.watchlist_service.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "watchlist_item",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "car_listing_id", "item_type"}),
    indexes = {
        @Index(name = "idx_wl_user", columnList = "user_id"),
        @Index(name = "idx_wl_car",  columnList = "car_listing_id")
    }
)
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class WatchlistItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "user_id",        nullable = false) private Long userId;
    @Column(name = "car_listing_id", nullable = false) private Long carListingId;
    @Column(name = "car_name",  length = 200) private String carName;
    @Column(name = "car_price", length = 50)  private String carPrice;
    @Column(name = "car_img_url", length = 512) private String carImgUrl;
    @Column(name = "car_brand", length = 50)  private String carBrand;
    @Column(name = "car_year")                private Integer carYear;
    @Column(name = "item_type", length = 20)  private String itemType;
    @CreationTimestamp @Column(name = "added_at", updatable = false) private LocalDateTime addedAt;
}
