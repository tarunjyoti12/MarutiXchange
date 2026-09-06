package com.marutixchange.notification_service.repository;

import com.marutixchange.notification_service.entity.NotificationLog;
import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationLogRepository extends JpaRepository<NotificationLog, UUID> {

    // ✅ CHANGED UUID → Long
    Page<NotificationLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<NotificationLog> findByStatusAndRetryCountLessThan(NotificationStatus status, int maxRetries);

    List<NotificationLog> findByReferenceIdAndReferenceType(UUID referenceId, String referenceType);

    // ✅ CHANGED UUID → Long
    long countByUserIdAndChannelAndCreatedAtAfter(
            Long userId, NotificationChannel channel, LocalDateTime after);

    @Modifying
    @Query("""
            UPDATE NotificationLog n
            SET n.status = :status,
                n.failureReason = :reason
            WHERE n.id = :id
            """)
    void updateStatus(
            @Param("id")     UUID id,
            @Param("status") NotificationStatus status,
            @Param("reason") String reason
    );
}