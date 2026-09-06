package com.marutixchange.user_service.service;

import com.marutixchange.user_service.dto.DocumentResponse;
import com.marutixchange.user_service.entity.UserDocument;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DocumentService {

    DocumentResponse uploadDocument(
            Long userId,
            MultipartFile file,
            UserDocument.DocumentType documentType);

    List<DocumentResponse> getUserDocuments(Long userId);
}