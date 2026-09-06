package com.marutixchange.payment_service.controller;

import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.service.PaymentService;
import com.marutixchange.payment_service.service.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/razorpay")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "${app.frontend.url}"})
public class RazorpayController {

    private final RazorpayService razorpayService;
    private final PaymentService paymentService;

    // ── Step 1: Create Razorpay Order ─────────────────────────────────────────
    @PostMapping("/create-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createOrder(
            @RequestBody RazorpayOrderRequest request) {

        log.info("Creating Razorpay order for carId: {}, buyerId: {}",
                request.getCarListingId(),
                request.getBuyerId());

        // Default token amount = ₹9,999 = 999900 paise
        if (request.getAmount() == null || request.getAmount() == 0) {
            request.setAmount(999900);
        }

        RazorpayOrderResponse response = razorpayService.createOrder(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Razorpay order created", response));
    }

    // ── Step 2: Verify Payment & Save Booking ─────────────────────────────────
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @RequestBody RazorpayVerifyRequest request) {

        log.info("Verifying payment: orderId={}, paymentId={}",
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId());

        // Verify signature
        boolean isValid = razorpayService.verifySignature(request);

        if (!isValid) {
            log.warn("Invalid payment signature for orderId: {}",
                    request.getRazorpayOrderId());

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(
                            "Payment verification failed — invalid signature"));
        }

        // Save payment to DB
        PaymentResponse paymentResponse = paymentService.initiatePayment(
                PaymentRequest.builder()
                        .carListingId(request.getCarListingId())
                        .buyerId(request.getBuyerId())
                        .sellerId(request.getSellerId())
                        .amount(request.getAmount() != null
                                ? request.getAmount()
                                : 9999.0)
                        .paymentMethod("RAZORPAY")
                        .paymentType("TOKEN")
                        .isTokenPayment(true)
                        .build()
        );

        // Confirm payment
        paymentService.confirmPayment(
                paymentResponse.getTransactionId());

        log.info("Payment verified and confirmed: {}",
                paymentResponse.getTransactionId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment verified and booking confirmed!",
                        paymentResponse
                )
        );
    }

    // ── Step 3: Handle Payment Failure ────────────────────────────────────────
    @PostMapping("/payment-failed")
    public ResponseEntity<ApiResponse<Void>> paymentFailed(
            @RequestBody RazorpayVerifyRequest request) {

        log.warn("Payment failed for orderId: {}",
                request.getRazorpayOrderId());

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment failure recorded",
                        null
                )
        );
    }
}