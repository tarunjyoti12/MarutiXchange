package com.marutixchange.notification_service.service;

import com.marutixchange.notification_service.client.RuleEngineClient;
import com.marutixchange.notification_service.dto.NotificationRequest;
import com.marutixchange.notification_service.dto.NotificationResponse;
import com.marutixchange.notification_service.dto.RuleContext;
import com.marutixchange.notification_service.entity.NotificationLog;
import com.marutixchange.notification_service.enums.NotificationChannel;
import com.marutixchange.notification_service.enums.NotificationStatus;
import com.marutixchange.notification_service.exception.NotificationNotFoundException;
import com.marutixchange.notification_service.repository.NotificationLogRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_RETRY = 3;

    private final NotificationLogRepository notificationLogRepository;
    private final RuleEngineClient ruleEngineClient;

    @Override
    @Transactional
    public NotificationResponse send(NotificationRequest request) {

        // 🔥 CALL RULE ENGINE
        RuleContext context = new RuleContext();
        context.setType("NOTIFICATION");
        context.setNotificationType(request.getNotificationType().name());
        context.setChannel(request.getChannel().name());
        context.setHour(LocalDateTime.now().getHour());

        RuleContext response = ruleEngineClient.evaluate(context);

        // ❌ BLOCK
        if (!response.isApproved()) {
            throw new RuntimeException("Notification blocked: " + response.getMessage());
        }

        // 🔄 OVERRIDE CHANNEL
        if (response.getChannel() != null) {
            request.setChannel(NotificationChannel.valueOf(response.getChannel()));
        }

        // ✅ FIXED: Proper builder + Long userId
        NotificationLog notif = NotificationLog.builder()
                .userId(request.getUserId())
                .notificationType(request.getNotificationType())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .title(request.getTitle())
                .body(request.getBody())
                .referenceId(request.getReferenceId())
                .referenceType(request.getReferenceType())
                .build();

        notif = notificationLogRepository.save(notif);

        try {
            log.info("📤 Notification sending → userId={} type={}",
                    request.getUserId(),
                    request.getNotificationType());

            notif.setStatus(NotificationStatus.SENT);
            notif.setSentAt(LocalDateTime.now());

        } catch (Exception ex) {
            notif.setStatus(NotificationStatus.FAILED);
            notif.setFailureReason(ex.getMessage());
        }

        notificationLogRepository.save(notif);
        return toResponse(notif);
    }

    // ✅ FIXED: UUID → Long
    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getByUserId(Long userId, Pageable pageable) {
        return notificationLogRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID notificationId) {
        return notificationLogRepository.findById(notificationId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotificationNotFoundException(
                        "Notification not found: " + notificationId));
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void retryFailed() {

        List<NotificationLog> failedList =
                notificationLogRepository.findByStatusAndRetryCountLessThan(
                        NotificationStatus.FAILED, MAX_RETRY);

        failedList.forEach(notif -> {
            try {
                notif.setStatus(NotificationStatus.RETRYING);
                notif.setRetryCount(notif.getRetryCount() + 1);
                notificationLogRepository.save(notif);

                notif.setStatus(NotificationStatus.SENT);
                notif.setSentAt(LocalDateTime.now());

            } catch (Exception ex) {
                notif.setStatus(NotificationStatus.FAILED);
                notif.setFailureReason(ex.getMessage());
            }

            notificationLogRepository.save(notif);
        });
    }

    private NotificationResponse toResponse(NotificationLog n) {
        return NotificationResponse.builder()
                .notificationId(n.getId())
                .userId(n.getUserId())
                .notificationType(n.getNotificationType())
                .channel(n.getChannel())
                .status(n.getStatus())
                .title(n.getTitle())
                .body(n.getBody())
                .referenceId(n.getReferenceId())
                .referenceType(n.getReferenceType())
                .sentAt(n.getSentAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}