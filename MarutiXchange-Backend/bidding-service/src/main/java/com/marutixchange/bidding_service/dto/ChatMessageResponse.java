package com.marutixchange.bidding_service.dto;

import com.marutixchange.bidding_service.entity.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {

    private Long id;
    private Long carListingId;
    private Long senderId;
    private Long receiverId;
    private String message;
    private String messageType;
    private Boolean isRead;
    private Double offerAmount;
    private String offerStatus;
    private LocalDateTime sentAt;

    public static ChatMessageResponse fromEntity(
            ChatMessage msg) {
        return ChatMessageResponse.builder()
                .id(msg.getId())
                .carListingId(msg.getCarListingId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .message(msg.getMessage())
                .messageType(
                        msg.getMessageType().name())
                .isRead(msg.getIsRead())
                .offerAmount(msg.getOfferAmount())
                .offerStatus(msg.getOfferStatus() != null
                        ? msg.getOfferStatus().name()
                        : null)
                .sentAt(msg.getSentAt())
                .build();
    }
}