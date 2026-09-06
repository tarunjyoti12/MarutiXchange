package com.marutixchange.payment_service.controller;

import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.service.EscrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/escrow")
@RequiredArgsConstructor
public class EscrowController {

    private final EscrowService escrowService;

    @PostMapping
    public ResponseEntity createEscrow(
            @Valid @RequestBody
            EscrowRequest request) {
        EscrowResponse response =
                escrowService.createEscrow(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Escrow created — funds held",
                        response));
    }

    @GetMapping("/{id}")
    public ResponseEntity getEscrowById(
            @PathVariable Long id) {
        EscrowResponse response =
                escrowService.getEscrowById(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Escrow fetched", response));
    }

    @GetMapping("/payment/{paymentId}")
    public ResponseEntity getEscrowByPaymentId(
            @PathVariable Long paymentId) {
        EscrowResponse response =
                escrowService
                        .getEscrowByPaymentId(paymentId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Escrow fetched", response));
    }

    @PatchMapping("/{id}/confirm-delivery")
    public ResponseEntity confirmDelivery(
            @PathVariable Long id) {
        EscrowResponse response =
                escrowService.confirmDelivery(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Delivery confirmed — " +
                                "funds ready for release",
                        response));
    }

    @PatchMapping("/{id}/release-funds")
    public ResponseEntity releaseFunds(
            @PathVariable Long id,
            @RequestParam(required = false)
            String notes) {
        EscrowResponse response =
                escrowService.releaseFunds(id, notes);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Funds released to seller",
                        response));
    }

    @PatchMapping("/{id}/dispute")
    public ResponseEntity raiseDispute(
            @PathVariable Long id) {
        EscrowResponse response =
                escrowService.raiseDispute(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dispute raised — " +
                                "funds frozen pending review",
                        response));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity getBuyerEscrows(
            @PathVariable Long buyerId) {
        List<EscrowResponse> escrows =
                escrowService.getBuyerEscrows(buyerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Buyer escrows fetched", escrows));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity getSellerEscrows(
            @PathVariable Long sellerId) {
        List<EscrowResponse> escrows =
                escrowService.getSellerEscrows(sellerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller escrows fetched", escrows));
    }
}