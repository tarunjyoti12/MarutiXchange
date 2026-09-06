package com.marutixchange.payment_service.controller;

import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.service.RefundService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @PostMapping
    public ResponseEntity initiateRefund(
            @Valid @RequestBody
            RefundRequest request) {
        RefundResponse response =
                refundService.initiateRefund(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Refund initiated", response));
    }

    @GetMapping("/{refundReference}")
    public ResponseEntity getRefund(
            @PathVariable String refundReference) {
        RefundResponse response =
                refundService.getRefundByReference(
                        refundReference);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Refund fetched", response));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity getBuyerRefunds(
            @PathVariable Long buyerId) {
        List<RefundResponse> refunds =
                refundService.getBuyerRefunds(buyerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Buyer refunds fetched", refunds));
    }
}