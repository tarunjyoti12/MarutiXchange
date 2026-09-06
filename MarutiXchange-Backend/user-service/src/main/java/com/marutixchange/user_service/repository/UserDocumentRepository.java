package com.marutixchange.user_service.repository;

import com.marutixchange.user_service.entity.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserDocumentRepository
        extends JpaRepository<UserDocument, Long> {

    // ================= BASIC =================

    List<UserDocument> findByUserId(Long userId);

    List<UserDocument> findByUserIdAndDocumentType(
            Long userId,
            UserDocument.DocumentType documentType);

    // ================= OPTIONAL (USEFUL) =================

    boolean existsByUserIdAndDocumentType(
            Long userId,
            UserDocument.DocumentType documentType);

    List<UserDocument> findByUserIdOrderByUploadedAtDesc(Long userId);
}