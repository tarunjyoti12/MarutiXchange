package com.marutixchange.user_service.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_documents",
        indexes = {
                @Index(name = "idx_user_document_user", columnList = "user_id"),
                @Index(name = "idx_user_document_type", columnList = "documentType")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ================= RELATION =================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ================= DOCUMENT =================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus =
            VerificationStatus.PENDING;

    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    // ================= ENUM =================

    public enum DocumentType {
        ID_PROOF,
        RC_BOOK,
        DRIVING_LICENSE,
        PASSPORT,
        AADHAR_CARD,
        PAN_CARD
    }

    public enum VerificationStatus {
        PENDING,
        VERIFIED,
        REJECTED
    }
}