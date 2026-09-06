package com.marutixchange.notification_service.entity;

import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationStatus;
import com.marutixchange.notification_service.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notification_log",
        indexes = {
                @Index(name = "idx_notif_user_id", columnList = "user_id"),
                @Index(name = "idx_notif_ref_id", columnList = "reference_id"),
                @Index(name = "idx_notif_status", columnList = "status"),
                @Index(name = "idx_notif_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // ✅ FIXED: UUID → Long
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "recipient", length = 512)
    private String recipient;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Builder.Default
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}