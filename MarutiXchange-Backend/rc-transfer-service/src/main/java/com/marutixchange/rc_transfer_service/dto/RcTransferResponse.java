package com.marutixchange.rc_transfer_service.dto;

import com.marutixchange.rc_transfer_service.entity.RcTransfer;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class RcTransferResponse {
    private Long id;
    private Long orderId;
    private Long carListingId;
    private Long buyerId;
    private Long sellerId;
    private String registrationNumber;
    private String vehicleClass;
    private String rtoOffice;
    private RcTransfer.TransferStatus status;
    private String rtoReferenceNumber;
    private LocalDateTime rtoSubmissionDate;
    private LocalDateTime expectedCompletionDate;
    private LocalDateTime completedDate;
    private String rejectionReason;
    private String notes;
    private boolean form29Uploaded;
    private boolean form30Uploaded;
    private boolean sellerIdProofUploaded;
    private boolean buyerIdProofUploaded;
    private boolean insuranceUploaded;
    private boolean pucUploaded;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static RcTransferResponse fromEntity(RcTransfer e) {
        return RcTransferResponse.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .carListingId(e.getCarListingId())
                .buyerId(e.getBuyerId())
                .sellerId(e.getSellerId())
                .registrationNumber(e.getRegistrationNumber())
                .vehicleClass(e.getVehicleClass())
                .rtoOffice(e.getRtoOffice())
                .status(e.getStatus())
                .rtoReferenceNumber(e.getRtoReferenceNumber())
                .rtoSubmissionDate(e.getRtoSubmissionDate())
                .expectedCompletionDate(e.getExpectedCompletionDate())
                .completedDate(e.getCompletedDate())
                .rejectionReason(e.getRejectionReason())
                .notes(e.getNotes())
                .form29Uploaded(e.getForm29Path() != null)
                .form30Uploaded(e.getForm30Path() != null)
                .sellerIdProofUploaded(e.getSellerIdProofPath() != null)
                .buyerIdProofUploaded(e.getBuyerIdProofPath() != null)
                .insuranceUploaded(e.getInsurancePath() != null)
                .pucUploaded(e.getPucPath() != null)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
