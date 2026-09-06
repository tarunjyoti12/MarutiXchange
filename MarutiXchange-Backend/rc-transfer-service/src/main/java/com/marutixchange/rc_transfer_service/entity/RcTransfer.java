package com.marutixchange.rc_transfer_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "rc_transfer", indexes = {
    @Index(name = "idx_rc_buyer",  columnList = "buyer_id"),
    @Index(name = "idx_rc_seller", columnList = "seller_id"),
    @Index(name = "idx_rc_order",  columnList = "order_id"),
    @Index(name = "idx_rc_status", columnList = "status")
})
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RcTransfer {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "car_listing_id", nullable = false)
    private Long carListingId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @Column(name = "registration_number", nullable = false, length = 20)
    private String registrationNumber;

    @Column(name = "vehicle_class", length = 50)
    private String vehicleClass;

    @Column(name = "rto_office", length = 100)
    private String rtoOffice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TransferStatus status;

    @Column(name = "form_29_path",         length = 512) private String form29Path;
    @Column(name = "form_30_path",         length = 512) private String form30Path;
    @Column(name = "seller_id_proof_path", length = 512) private String sellerIdProofPath;
    @Column(name = "buyer_id_proof_path",  length = 512) private String buyerIdProofPath;
    @Column(name = "insurance_path",       length = 512) private String insurancePath;
    @Column(name = "puc_path",             length = 512) private String pucPath;
    @Column(name = "rto_ack_path",         length = 512) private String rtoAcknowledgementPath;

    @Column(name = "rto_reference_number",   length = 50) private String rtoReferenceNumber;
    @Column(name = "rto_submission_date")                 private LocalDateTime rtoSubmissionDate;
    @Column(name = "expected_completion_date")            private LocalDateTime expectedCompletionDate;
    @Column(name = "completed_date")                      private LocalDateTime completedDate;

    @Column(name = "rejection_reason", columnDefinition = "TEXT") private String rejectionReason;
    @Column(name = "agent_id")         private Long agentId;
    @Column(name = "notes", columnDefinition = "TEXT") private String notes;

    @CreationTimestamp @Column(name = "created_at", updatable = false) private LocalDateTime createdAt;
    @UpdateTimestamp   @Column(name = "updated_at")                    private LocalDateTime updatedAt;
    @Version           private Long version;

    public enum TransferStatus {
        INITIATED, DOCUMENTS_PENDING, DOCUMENTS_SUBMITTED,
        RTO_SUBMITTED, RTO_PROCESSING, COMPLETED, REJECTED, CANCELLED
    }
}
