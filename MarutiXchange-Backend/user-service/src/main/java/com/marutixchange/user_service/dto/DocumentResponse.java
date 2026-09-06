package com.marutixchange.user_service.dto;

import com.marutixchange.user_service.entity.UserDocument;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DocumentResponse {

    private Long id;
    private Long userId;
    private String documentType;
    private String documentUrl;
    private String verificationStatus;
    private LocalDateTime uploadedAt;

    public static DocumentResponse fromEntity(UserDocument doc) {
        if (doc == null) return null;

        return DocumentResponse.builder()
                .id(doc.getId())
                .userId(doc.getUser() != null ? doc.getUser().getId() : null)
                .documentType(doc.getDocumentType() != null ? doc.getDocumentType().name() : null)
                .documentUrl(doc.getDocumentUrl())
                .verificationStatus(doc.getVerificationStatus() != null
                        ? doc.getVerificationStatus().name()
                        : null)
                .uploadedAt(doc.getUploadedAt())
                .build();
    }
}