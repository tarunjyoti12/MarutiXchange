package com.marutixchange.payment_service.dto;

import com.marutixchange.payment_service.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PaymentResponse {

    private Long id;
    private String transactionId;
    private Long carListingId;
    private Long auctionId;
    private Long buyerId;
    private Long sellerId;
    private Double amount;
    private Double tokenAmount;
    private Double remainingAmount;
    private String paymentMethod;
    private String paymentType;
    private String status;
    private String invoiceNumber;
    private String upiId;
    private String upiApp;
    private String bankName;
    private String failureReason;
    private Boolean isTokenPayment;
    private LocalDateTime timeoutAt;
    private LocalDateTime createdAt;
    private String paymentId;

    // ✅ ADD THIS
    private List<String> warnings;

    // ✅ OLD METHOD (keep this)
    public static PaymentResponse fromEntity(Payment p) {
        return fromEntity(p, null);
    }

    // ✅ NEW METHOD
    public static PaymentResponse fromEntity(
            Payment p,
            List<String> warnings) {

        return PaymentResponse.builder()
                .id(p.getId())
                .transactionId(p.getTransactionId())
                .carListingId(p.getCarListingId())
                .auctionId(p.getAuctionId())
                .buyerId(p.getBuyerId())
                .sellerId(p.getSellerId())
                .amount(p.getAmount())
                .tokenAmount(p.getTokenAmount())
                .remainingAmount(p.getRemainingAmount())
                .paymentMethod(p.getPaymentMethod() != null ? p.getPaymentMethod().name() : null)
                .paymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .invoiceNumber(p.getInvoiceNumber())
                .upiId(p.getUpiId())
                .upiApp(p.getUpiApp())
                .bankName(p.getBankName())
                .failureReason(p.getFailureReason())
                .isTokenPayment(p.getIsTokenPayment())
                .timeoutAt(p.getTimeoutAt())
                .createdAt(p.getCreatedAt())
                .warnings(warnings) // ✅ important
                .paymentId(p.getTransactionId())
                .build();
    }
}