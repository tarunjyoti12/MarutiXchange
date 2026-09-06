package com.marutixchange.rc_transfer_service.controller;

import com.marutixchange.rc_transfer_service.dto.ApiResponse;
import com.marutixchange.rc_transfer_service.dto.RcTransferRequest;
import com.marutixchange.rc_transfer_service.dto.RcTransferResponse;
import com.marutixchange.rc_transfer_service.entity.RcTransfer;
import com.marutixchange.rc_transfer_service.service.RcTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/rc-transfers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "RC Transfer", description = "Vehicle RC ownership transfer management")
@SecurityRequirement(name = "Bearer Authentication")
public class RcTransferController {

    private final RcTransferService rcTransferService;

    @PostMapping
    @Operation(summary = "Initiate a new RC transfer after order completion")
    public ResponseEntity<ApiResponse<RcTransferResponse>> initiateTransfer(
            @Valid @RequestBody RcTransferRequest request) {
        log.info("POST /api/v1/rc-transfers orderId={}", request.getOrderId());
        return ResponseEntity.ok(
            ApiResponse.success(rcTransferService.initiateTransfer(request),
                "RC transfer initiated successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get RC transfer by ID")
    public ResponseEntity<ApiResponse<RcTransferResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
            ApiResponse.success(rcTransferService.getById(id), "RC transfer fetched"));
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Get RC transfer by order ID")
    public ResponseEntity<ApiResponse<RcTransferResponse>> getByOrderId(
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
            ApiResponse.success(rcTransferService.getByOrderId(orderId), "RC transfer fetched"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all RC transfers for a user (buyer or seller)")
    public ResponseEntity<ApiResponse<Page<RcTransferResponse>>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
            ApiResponse.success(rcTransferService.getTransfersByUser(userId, pageable),
                "RC transfers fetched"));
    }

    @PostMapping("/{id}/documents")
    @Operation(summary = "Upload a document (FORM_29, FORM_30, SELLER_ID_PROOF, BUYER_ID_PROOF, INSURANCE, PUC)")
    public ResponseEntity<ApiResponse<RcTransferResponse>> uploadDocument(
            @PathVariable Long id,
            @RequestParam String documentType,
            @RequestParam MultipartFile file,
            @RequestParam Long requesterId) {
        log.info("POST /api/v1/rc-transfers/{}/documents type={}", id, documentType);
        return ResponseEntity.ok(
            ApiResponse.success(
                rcTransferService.uploadDocument(id, documentType, file, requesterId),
                "Document uploaded successfully"));
    }

    @PutMapping("/{id}/rto-submit")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Submit documents to RTO (Agent/Admin only)")
    public ResponseEntity<ApiResponse<RcTransferResponse>> submitToRto(
            @PathVariable Long id,
            @RequestParam String rtoOffice,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(
            ApiResponse.success(rcTransferService.submitToRto(id, rtoOffice, agentId),
                "Submitted to RTO"));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Mark RC transfer as completed (Agent/Admin only)")
    public ResponseEntity<ApiResponse<RcTransferResponse>> completeTransfer(
            @PathVariable Long id,
            @RequestParam String rtoReferenceNumber,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                rcTransferService.completeTransfer(id, rtoReferenceNumber, agentId),
                "RC transfer completed"));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('AGENT')")
    @Operation(summary = "Update RC transfer status (Agent/Admin only)")
    public ResponseEntity<ApiResponse<RcTransferResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam RcTransfer.TransferStatus status,
            @RequestParam(required = false) String notes,
            @RequestParam Long agentId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                rcTransferService.updateStatus(id, status, notes, agentId),
                "Status updated"));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel an RC transfer")
    public ResponseEntity<ApiResponse<RcTransferResponse>> cancelTransfer(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam Long requesterId) {
        return ResponseEntity.ok(
            ApiResponse.success(
                rcTransferService.cancelTransfer(id, reason, requesterId),
                "RC transfer cancelled"));
    }
}
