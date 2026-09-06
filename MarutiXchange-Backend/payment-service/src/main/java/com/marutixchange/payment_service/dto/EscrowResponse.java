package com.marutixchange.payment_service.dto;

import com.marutixchange.payment_service.entity.Escrow;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class EscrowResponse {

    private Long id;
    private String escrowReference;
    private Long paymentId;
    private Long carListingId;
    private Long buyerId;
    private Long sellerId;
    private Double heldAmount;
    private String status;
    private LocalDateTime deliveryConfirmedAt;
    private LocalDateTime fundsReleasedAt;
    private LocalDateTime autoReleaseAt;
    private String releaseNotes;
    private LocalDateTime createdAt;

    public static EscrowResponse fromEntity(
            Escrow e) {
        return EscrowResponse.builder()
                .id(e.getId())
                .escrowReference(e.getEscrowReference())
                .paymentId(e.getPaymentId())
                .carListingId(e.getCarListingId())
                .buyerId(e.getBuyerId())
                .sellerId(e.getSellerId())
                .heldAmount(e.getHeldAmount())
                .status(e.getStatus().name())
                .deliveryConfirmedAt(
                        e.getDeliveryConfirmedAt())
                .fundsReleasedAt(
                        e.getFundsReleasedAt())
                .autoReleaseAt(e.getAutoReleaseAt())
                .releaseNotes(e.getReleaseNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }
}