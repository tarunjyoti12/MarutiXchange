package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.dto.*;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Service
@Slf4j
public class RazorpayService {

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // ── Create Razorpay Order ─────────────────────────────────────────────────
    public RazorpayOrderResponse createOrder(RazorpayOrderRequest request) {

        try {

            RazorpayClient client =
                    new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();

            // amount in paise
            orderRequest.put("amount",
                    request.getAmount());

            orderRequest.put(
                    "currency",
                    request.getCurrency() != null
                            ? request.getCurrency()
                            : "INR"
            );

            orderRequest.put(
                    "receipt",
                    "rcpt_" +
                            request.getCarListingId() +
                            "_" +
                            request.getBuyerId()
            );

            orderRequest.put("payment_capture", 1);

            Order order =
                    client.orders.create(orderRequest);

            // FIXED LOGGER ISSUE
            log.info(
                    "Razorpay order created: "
                            + order.get("id")
            );

            return RazorpayOrderResponse.builder()
                    .orderId(order.get("id").toString())
                    .amount(request.getAmount())
                    .currency("INR")
                    .receipt(order.get("receipt").toString())
                    .keyId(keyId)
                    .build();

        } catch (Exception e) {

            log.error(
                    "Failed to create Razorpay order: "
                            + e.getMessage()
            );

            throw new RuntimeException(
                    "Failed to create payment order: "
                            + e.getMessage()
            );
        }
    }

    // ── Verify Razorpay Payment Signature ─────────────────────────────────────
    public boolean verifySignature(
            RazorpayVerifyRequest request
    ) {

        try {

            String payload =
                    request.getRazorpayOrderId()
                            + "|"
                            + request.getRazorpayPaymentId();

            Mac mac =
                    Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            keySecret.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA256"
                    );

            mac.init(secretKey);

            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            String generatedSignature =
                    HexFormat.of().formatHex(hash);

            boolean isValid =
                    generatedSignature.equals(
                            request.getRazorpaySignature()
                    );

            log.info(
                    "Signature verification: "
                            + (isValid
                            ? "SUCCESS"
                            : "FAILED")
            );

            return isValid;

        } catch (Exception e) {

            log.error(
                    "Signature verification error: "
                            + e.getMessage()
            );

            return false;
        }
    }
}