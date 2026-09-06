package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.ChatMessageRequest;
import com.marutixchange.bidding_service.dto.ChatMessageResponse;
import com.marutixchange.bidding_service.entity.ChatMessage;
import com.marutixchange.bidding_service.exception.UnauthorizedAccessException;
import com.marutixchange.bidding_service.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId) {

        ChatMessage message = ChatMessage.builder()
                .carListingId(request.getCarListingId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .message(request.getMessage())
                .messageType(ChatMessage.MessageType.TEXT)
                .isRead(false)
                .build();

        return ChatMessageResponse.fromEntity(chatMessageRepository.save(message));
    }

    @Override
    public List<ChatMessageResponse> getConversation(Long carListingId, Long userId, Long otherUserId) {

        return chatMessageRepository.findConversation(carListingId, userId, otherUserId)
                .stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageResponse sendOffer(ChatMessageRequest request, Long senderId) {

        ChatMessage message = ChatMessage.builder()
                .carListingId(request.getCarListingId())
                .senderId(senderId)
                .receiverId(request.getReceiverId())
                .message("Offer: ₹" + request.getOfferAmount())
                .messageType(ChatMessage.MessageType.OFFER)
                .offerAmount(request.getOfferAmount())
                .offerStatus(ChatMessage.OfferStatus.PENDING)
                .isRead(false)
                .build();

        return ChatMessageResponse.fromEntity(chatMessageRepository.save(message));
    }

    @Override
    @Transactional
    public ChatMessageResponse respondToOffer(Long messageId, boolean accepted, Long userId) {

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found: " + messageId));

        // 🔐 Security check
        if (!message.getReceiverId().equals(userId)) {
            throw new UnauthorizedAccessException("You are not allowed to respond to this offer");
        }

        message.setOfferStatus(accepted
                ? ChatMessage.OfferStatus.ACCEPTED
                : ChatMessage.OfferStatus.REJECTED);

        return ChatMessageResponse.fromEntity(chatMessageRepository.save(message));
    }

    @Override
    public long getUnreadCount(Long userId) {
        return chatMessageRepository.countByReceiverIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public void markAsRead(Long receiverId, Long senderId) {
        chatMessageRepository.markMessagesAsRead(receiverId, senderId);
    }
}