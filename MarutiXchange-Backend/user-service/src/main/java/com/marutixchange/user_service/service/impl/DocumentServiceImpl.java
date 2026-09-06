package com.marutixchange.user_service.service.impl;

import com.marutixchange.user_service.dto.DocumentResponse;
import com.marutixchange.user_service.entity.User;
import com.marutixchange.user_service.entity.UserDocument;
import com.marutixchange.user_service.exception.UserNotFoundException;
import com.marutixchange.user_service.repository.UserDocumentRepository;
import com.marutixchange.user_service.repository.UserRepository;
import com.marutixchange.user_service.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final UserRepository userRepository;
    private final UserDocumentRepository documentRepository;

    @Value("${app.upload.dir:uploads/documents}")
    private String uploadDir;

    @Override
    public DocumentResponse uploadDocument(
            Long userId,
            MultipartFile file,
            UserDocument.DocumentType documentType) {

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));

            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            UserDocument document = UserDocument.builder()
                    .user(user)
                    .documentType(documentType)
                    .documentUrl(uploadDir + "/" + fileName)
                    .verificationStatus(UserDocument.VerificationStatus.PENDING)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            return DocumentResponse.fromEntity(documentRepository.save(document));

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }
    }

    @Override
    public List<DocumentResponse> getUserDocuments(Long userId) {
        return documentRepository.findByUserId(userId)
                .stream()
                .map(DocumentResponse::fromEntity)
                .toList();
    }
}