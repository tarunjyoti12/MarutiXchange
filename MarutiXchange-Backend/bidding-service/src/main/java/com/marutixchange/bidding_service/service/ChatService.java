package com.marutixchange.bidding_service.service;

import com.marutixchange.bidding_service.dto.ChatMessageRequest;
import com.marutixchange.bidding_service.dto.ChatMessageResponse;

import java.util.List;

public interface ChatService {

    ChatMessageResponse sendMessage(ChatMessageRequest request, Long senderId);

    List<ChatMessageResponse> getConversation(Long carListingId, Long userId, Long otherUserId);

    ChatMessageResponse sendOffer(ChatMessageRequest request, Long senderId);

    ChatMessageResponse respondToOffer(Long messageId, boolean accepted, Long userId);

    long getUnreadCount(Long userId);

    void markAsRead(Long receiverId, Long senderId);
}