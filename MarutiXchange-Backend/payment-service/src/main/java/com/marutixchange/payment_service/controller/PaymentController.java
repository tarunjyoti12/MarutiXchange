package com.marutixchange.payment_service.controller;

import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity initiatePayment(
            @Valid @RequestBody
            PaymentRequest request) {
        PaymentResponse response =
                paymentService.initiatePayment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{transactionId}/confirm")
    public ResponseEntity confirmPayment(
            @PathVariable String transactionId) {
        PaymentResponse response =
                paymentService.confirmPayment(
                        transactionId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment confirmed successfully",
                        response));
    }

    @PatchMapping("/{transactionId}/fail")
    public ResponseEntity failPayment(
            @PathVariable String transactionId,
            @RequestParam String reason) {
        PaymentResponse response =
                paymentService.failPayment(
                        transactionId, reason);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment marked as failed",
                        response));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity getPayment(
            @PathVariable String transactionId) {
        PaymentResponse response =
                paymentService
                        .getPaymentByTransactionId(
                                transactionId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment fetched", response));
    }

    @GetMapping("/buyer/{buyerId}")
    public ResponseEntity getBuyerPayments(
            @PathVariable Long buyerId) {
        List<PaymentResponse> payments =
                paymentService.getBuyerPayments(buyerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Buyer payments fetched",
                        payments));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity getSellerPayments(
            @PathVariable Long sellerId) {
        List<PaymentResponse> payments =
                paymentService
                        .getSellerPayments(sellerId);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Seller payments fetched",
                        payments));
    }

    @PostMapping("/emi/calculate")
    public ResponseEntity calculateEmi(
            @Valid @RequestBody
            EmiCalculatorRequest request) {
        EmiCalculatorResponse response =
                paymentService.calculateEmi(request);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "EMI calculated", response));
    }
}