package com.marutixchange.bidding_service.controller;

import com.marutixchange.bidding_service.dto.*;
import com.marutixchange.bidding_service.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @Valid @RequestBody ChatMessageRequest request,
            @RequestHeader("X-User-Id") Long senderId) {

        ChatMessageResponse response = chatService.sendMessage(request, senderId);

        return ResponseEntity.ok(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/conversation")
    public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> getConversation(
            @RequestParam Long carListingId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long otherUserId) {

        List<ChatMessageResponse> response =
                chatService.getConversation(carListingId, userId, otherUserId);

        return ResponseEntity.ok(ApiResponse.success("Conversation fetched", response));
    }

    @PostMapping("/offer")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendOffer(
            @Valid @RequestBody ChatMessageRequest request,
            @RequestHeader("X-User-Id") Long senderId) {

        ChatMessageResponse response = chatService.sendOffer(request, senderId);

        return ResponseEntity.ok(ApiResponse.success("Offer sent", response));
    }

    @PatchMapping("/offer/{messageId}/respond")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> respondToOffer(
            @PathVariable Long messageId,
            @RequestParam boolean accepted,
            @RequestHeader("X-User-Id") Long userId) {

        ChatMessageResponse response =
                chatService.respondToOffer(messageId, accepted, userId);

        return ResponseEntity.ok(ApiResponse.success(
                accepted ? "Offer accepted" : "Offer rejected",
                response));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @RequestHeader("X-User-Id") Long userId) {

        Long count = chatService.getUnreadCount(userId);

        return ResponseEntity.ok(ApiResponse.success("Unread count fetched", count));
    }

    @PatchMapping("/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @RequestHeader("X-User-Id") Long receiverId,
            @RequestParam Long senderId) {

        chatService.markAsRead(receiverId, senderId);

        return ResponseEntity.ok(ApiResponse.success("Messages marked as read", null));
    }
}