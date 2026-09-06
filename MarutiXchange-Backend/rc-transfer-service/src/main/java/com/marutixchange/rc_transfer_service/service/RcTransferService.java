package com.marutixchange.rc_transfer_service.service;

import com.marutixchange.rc_transfer_service.dto.RcTransferRequest;
import com.marutixchange.rc_transfer_service.dto.RcTransferResponse;
import com.marutixchange.rc_transfer_service.entity.RcTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface RcTransferService {
    RcTransferResponse initiateTransfer(RcTransferRequest request);
    RcTransferResponse getById(Long id);
    RcTransferResponse getByOrderId(Long orderId);
    Page<RcTransferResponse> getTransfersByUser(Long userId, Pageable pageable);
    RcTransferResponse uploadDocument(Long transferId, String documentType, MultipartFile file, Long requesterId);
    RcTransferResponse updateStatus(Long transferId, RcTransfer.TransferStatus status, String notes, Long agentId);
    RcTransferResponse submitToRto(Long transferId, String rtoOffice, Long agentId);
    RcTransferResponse completeTransfer(Long transferId, String rtoReferenceNumber, Long agentId);
    RcTransferResponse cancelTransfer(Long transferId, String reason, Long requesterId);
}
