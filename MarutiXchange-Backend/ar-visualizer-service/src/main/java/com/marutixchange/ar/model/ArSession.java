package com.marutixchange.ar.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ar_sessions", indexes = {
    @Index(name = "idx_ar_sessions_car_id",  columnList = "car_id"),
    @Index(name = "idx_ar_sessions_user_id", columnList = "user_id"),
    @Index(name = "idx_ar_sessions_created", columnList = "created_at")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ArSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true, nullable = false, length = 64)
    private String sessionId;

    @Column(name = "car_id", nullable = false)
    private Long carId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "car_name", length = 100)
    private String carName;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "color_changes")
    private Integer colorChanges;

    @Column(name = "resize_count")
    private Integer resizeCount;

    @Column(name = "screenshot_taken")
    private Boolean screenshotTaken;

    @Column(name = "screenshot_url", length = 512)
    private String screenshotUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ArSessionStatus status;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum ArSessionStatus {
        ACTIVE, COMPLETED, ABANDONED
    }
}
