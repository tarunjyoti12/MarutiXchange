package com.marutixchange.car_listing_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "car_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)   // ✅ FIX
    @JoinColumn(name = "car_listing_id")
    private CarListing carListing;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "is_primary")
    @Builder.Default
    private Boolean isPrimary = false;

    @CreationTimestamp
    private LocalDateTime uploadedAt;
}