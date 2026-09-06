package com.marutixchange.bidding_service.repository;

import com.marutixchange.bidding_service.entity.ChatMessage;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE " +
            "m.carListingId = :carListingId AND " +
            "((m.senderId = :userId1 AND m.receiverId = :userId2) OR " +
            "(m.senderId = :userId2 AND m.receiverId = :userId1)) " +
            "ORDER BY m.sentAt ASC")
    List<ChatMessage> findConversation(
            @Param("carListingId") Long carListingId,
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2);

    List<ChatMessage> findByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    long countByReceiverIdAndIsRead(Long receiverId, Boolean isRead);

    // 🔥 Bulk mark as read
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiverId = :receiverId AND m.senderId = :senderId")
    void markMessagesAsRead(@Param("receiverId") Long receiverId,
                            @Param("senderId") Long senderId);
}