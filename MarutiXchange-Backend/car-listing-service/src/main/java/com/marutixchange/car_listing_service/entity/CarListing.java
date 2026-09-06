package com.marutixchange.car_listing_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "car_listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @NotBlank
    @Column(nullable = false)
    private String brand;

    @NotBlank
    @Column(nullable = false)
    private String model;

    @NotNull
    @Column(nullable = false)
    private Integer year;

    private String variant;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", nullable = false)
    private FuelType fuelType;

    @Enumerated(EnumType.STRING)
    @Column(name = "transmission", nullable = false)
    private Transmission transmission;

    private String color;
    private Integer mileage;
    private Integer engineCc;

    private Integer ownerNumber;
    private String registrationNumber;
    private String vinNumber;
    private String rcNumber;

    @NotNull
    @Column(name = "asking_price", nullable = false)
    private Double askingPrice;

    private Double marketPrice;
    private Double minPrice;

    private String city;
    private String state;
    private String pincode;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ListingStatus status = ListingStatus.PENDING;

    @Builder.Default
    private Boolean isCertified = false;

    @Builder.Default
    private Boolean isBoosted = false;

    @Builder.Default
    private Integer viewCount = 0;

    @Builder.Default
    private Integer inquiryCount = 0;

    private String serviceHistoryUrl;
    private String insuranceValidTill;

    @Builder.Default
    private Boolean testDriveAvailable = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime soldAt;

    @OneToMany(mappedBy = "carListing",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore   // ✅ VERY IMPORTANT FIX
    private List<CarImage> images;

    @OneToMany(mappedBy = "carListing",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @JsonIgnore   // ✅ VERY IMPORTANT FIX
    private List<CarReview> reviews;

    public enum FuelType {
        PETROL, DIESEL, CNG, ELECTRIC, HYBRID
    }

    public enum Transmission {
        MANUAL, AUTOMATIC, AMT, CVT
    }

    public enum ListingStatus {
        PENDING,
        ACTIVE,
        SOLD,
        REJECTED,
        EXPIRED,
        BOOSTED
    }
}