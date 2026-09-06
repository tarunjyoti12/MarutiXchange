package com.marutixchange.payment_service.dto;

import com.marutixchange.payment_service.entity.Refund;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class RefundResponse {

    private Long id;
    private String refundReference;
    private String transactionId;
    private Long paymentId;
    private Long buyerId;
    private Double originalAmount;
    private Double refundAmount;
    private Double cancellationFee;
    private String reason;
    private String status;
    private Boolean isWithinFreeWindow;
    private Long hoursSincePayment;
    private String message;
    private LocalDateTime createdAt;

    public static RefundResponse fromEntity(
            Refund r) {
        return RefundResponse.builder()
                .id(r.getId())
                .refundReference(
                        r.getRefundReference())
                .transactionId(r.getTransactionId())
                .paymentId(r.getPaymentId())
                .buyerId(r.getBuyerId())
                .originalAmount(r.getOriginalAmount())
                .refundAmount(r.getRefundAmount())
                .cancellationFee(
                        r.getCancellationFee())
                .reason(r.getReason().name())
                .status(r.getStatus().name())
                .isWithinFreeWindow(
                        r.getIsWithinFreeWindow())
                .hoursSincePayment(
                        r.getHoursSincePayment())
                .createdAt(r.getCreatedAt())
                .build();
    }
}