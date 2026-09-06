package com.marutixchange.order_service.client;

import com.marutixchange.order_service.dto.PaymentRequest;
import com.marutixchange.order_service.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "payment-service")
public interface PaymentClient {

    @PostMapping("/api/v1/payments/initiate") // ✅ FIXED
    PaymentResponse createPayment(@RequestBody PaymentRequest request);
}