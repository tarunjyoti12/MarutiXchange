package com.marutixchange.car_listing_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "car_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)   // ✅ FIX
    @JoinColumn(name = "car_listing_id")
    private CarListing carListing;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;
}