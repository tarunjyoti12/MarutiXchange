package com.marutixchange.rc_transfer_service.service.impl;

import com.marutixchange.rc_transfer_service.dto.RcTransferRequest;
import com.marutixchange.rc_transfer_service.dto.RcTransferResponse;
import com.marutixchange.rc_transfer_service.entity.RcTransfer;
import com.marutixchange.rc_transfer_service.entity.RcTransfer.TransferStatus;
import com.marutixchange.rc_transfer_service.exception.RcTransferNotFoundException;
import com.marutixchange.rc_transfer_service.exception.InvalidOperationException;
import com.marutixchange.rc_transfer_service.kafka.RcTransferEventProducer;
import com.marutixchange.rc_transfer_service.repository.RcTransferRepository;
import com.marutixchange.rc_transfer_service.service.RcTransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RcTransferServiceImpl implements RcTransferService {

    private final RcTransferRepository rcTransferRepository;
    private final RcTransferEventProducer eventProducer;

    @Value("${app.upload.dir:uploads/rc-documents}")
    private String uploadDir;

    @Override
    @Transactional
    public RcTransferResponse initiateTransfer(RcTransferRequest request) {
        log.info("Initiating RC transfer for orderId={}", request.getOrderId());

        // Prevent duplicate
        rcTransferRepository.findByOrderId(request.getOrderId()).ifPresent(existing -> {
            throw new InvalidOperationException(
                "RC transfer already exists for orderId=" + request.getOrderId());
        });

        RcTransfer transfer = RcTransfer.builder()
                .orderId(request.getOrderId())
                .carListingId(request.getCarListingId())
                .buyerId(request.getBuyerId())
                .sellerId(request.getSellerId())
                .registrationNumber(request.getRegistrationNumber().toUpperCase().trim())
                .vehicleClass(request.getVehicleClass())
                .rtoOffice(request.getRtoOffice())
                .status(TransferStatus.INITIATED)
                .expectedCompletionDate(LocalDateTime.now().plusDays(30))
                .build();

        RcTransfer saved = rcTransferRepository.save(transfer);
        log.info("RC transfer initiated id={}", saved.getId());

        // Notify both parties via Kafka
        eventProducer.publishTransferInitiated(
                request.getBuyerId(), request.getSellerId(),
                saved.getId(), request.getRegistrationNumber());

        return RcTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RcTransferResponse getById(Long id) {
        return RcTransferResponse.fromEntity(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RcTransferResponse getByOrderId(Long orderId) {
        return RcTransferResponse.fromEntity(
            rcTransferRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RcTransferNotFoundException(
                    "RC Transfer not found for orderId=" + orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RcTransferResponse> getTransfersByUser(Long userId, Pageable pageable) {
        return rcTransferRepository.findAllByUserId(userId, pageable)
                .map(RcTransferResponse::fromEntity);
    }

    @Override
    @Transactional
    public RcTransferResponse uploadDocument(Long transferId, String documentType,
                                              MultipartFile file, Long requesterId) {
        RcTransfer transfer = findOrThrow(transferId);
        validateParticipant(transfer, requesterId);

        String filePath = saveFile(file, documentType, transferId);

        switch (documentType.toUpperCase()) {
            case "FORM_29"         -> transfer.setForm29Path(filePath);
            case "FORM_30"         -> transfer.setForm30Path(filePath);
            case "SELLER_ID_PROOF" -> transfer.setSellerIdProofPath(filePath);
            case "BUYER_ID_PROOF"  -> transfer.setBuyerIdProofPath(filePath);
            case "INSURANCE"       -> transfer.setInsurancePath(filePath);
            case "PUC"             -> transfer.setPucPath(filePath);
            default -> throw new InvalidOperationException("Unknown document type: " + documentType);
        }

        // Auto-advance status when all mandatory docs are uploaded
        if (allMandatoryDocsUploaded(transfer)
                && transfer.getStatus() == TransferStatus.INITIATED) {
            transfer.setStatus(TransferStatus.DOCUMENTS_PENDING);
        }

        RcTransfer saved = rcTransferRepository.save(transfer);
        log.info("Document {} uploaded for transferId={}", documentType, transferId);
        return RcTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RcTransferResponse updateStatus(Long transferId, TransferStatus status,
                                            String notes, Long agentId) {
        RcTransfer transfer = findOrThrow(transferId);
        TransferStatus oldStatus = transfer.getStatus();
        transfer.setStatus(status);
        transfer.setNotes(notes);
        transfer.setAgentId(agentId);

        if (status == TransferStatus.COMPLETED) {
            transfer.setCompletedDate(LocalDateTime.now());
        }

        RcTransfer saved = rcTransferRepository.save(transfer);
        log.info("RC transfer {} status: {} -> {}", transferId, oldStatus, status);
        return RcTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RcTransferResponse submitToRto(Long transferId, String rtoOffice, Long agentId) {
        RcTransfer transfer = findOrThrow(transferId);

        if (!allMandatoryDocsUploaded(transfer)) {
            throw new InvalidOperationException(
                "Cannot submit to RTO — mandatory documents missing.");
        }

        transfer.setStatus(TransferStatus.RTO_SUBMITTED);
        transfer.setRtoOffice(rtoOffice);
        transfer.setAgentId(agentId);
        transfer.setRtoSubmissionDate(LocalDateTime.now());
        transfer.setExpectedCompletionDate(LocalDateTime.now().plusDays(21));

        RcTransfer saved = rcTransferRepository.save(transfer);
        eventProducer.publishRtoSubmitted(transfer.getBuyerId(), transfer.getSellerId(),
                transferId, rtoOffice);
        return RcTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RcTransferResponse completeTransfer(Long transferId,
                                                String rtoReferenceNumber, Long agentId) {
        RcTransfer transfer = findOrThrow(transferId);
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setRtoReferenceNumber(rtoReferenceNumber);
        transfer.setCompletedDate(LocalDateTime.now());
        transfer.setAgentId(agentId);

        RcTransfer saved = rcTransferRepository.save(transfer);
        eventProducer.publishTransferCompleted(transfer.getBuyerId(), transfer.getSellerId(),
                transferId, transfer.getRegistrationNumber());
        log.info("RC transfer completed id={}", transferId);
        return RcTransferResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public RcTransferResponse cancelTransfer(Long transferId, String reason, Long requesterId) {
        RcTransfer transfer = findOrThrow(transferId);
        validateParticipant(transfer, requesterId);

        if (transfer.getStatus() == TransferStatus.COMPLETED) {
            throw new InvalidOperationException("Cannot cancel a completed RC transfer.");
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setRejectionReason(reason);
        return RcTransferResponse.fromEntity(rcTransferRepository.save(transfer));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private RcTransfer findOrThrow(Long id) {
        return rcTransferRepository.findById(id)
                .orElseThrow(() -> new RcTransferNotFoundException(
                    "RC Transfer not found: " + id));
    }

    private void validateParticipant(RcTransfer transfer, Long userId) {
        if (!transfer.getBuyerId().equals(userId) && !transfer.getSellerId().equals(userId)) {
            throw new InvalidOperationException(
                "User " + userId + " is not a participant in this RC transfer.");
        }
    }

    private boolean allMandatoryDocsUploaded(RcTransfer t) {
        return t.getForm29Path() != null
            && t.getForm30Path() != null
            && t.getSellerIdProofPath() != null
            && t.getBuyerIdProofPath() != null;
    }

    private String saveFile(MultipartFile file, String docType, Long transferId) {
        try {
            String ext = getExtension(file.getOriginalFilename());
            String filename = "transfer_" + transferId + "_" + docType + "_"
                    + UUID.randomUUID().toString().substring(0, 8) + ext;
            Path dir = Paths.get(uploadDir, String.valueOf(transferId));
            Files.createDirectories(dir);
            Path target = dir.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return target.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document: " + e.getMessage(), e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".pdf";
        return filename.substring(filename.lastIndexOf('.'));
    }
}
