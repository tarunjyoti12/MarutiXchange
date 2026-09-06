package com.marutixchange.bidding_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages",
        indexes = {
                @Index(name = "idx_chat_car", columnList = "car_listing_id"),
                @Index(name = "idx_chat_sender", columnList = "sender_id"),
                @Index(name = "idx_chat_receiver", columnList = "receiver_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "offer_amount")
    private Double offerAmount;

    @Enumerated(EnumType.STRING)
    private OfferStatus offerStatus;

    @CreationTimestamp
    private LocalDateTime sentAt;

    public enum MessageType {
        TEXT,
        OFFER,
        COUNTER_OFFER,
        ACCEPTED,
        REJECTED,
        SYSTEM
    }

    public enum OfferStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        COUNTERED
    }
}