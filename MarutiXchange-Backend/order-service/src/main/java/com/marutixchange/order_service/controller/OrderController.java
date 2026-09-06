package com.marutixchange.order_service.controller;

import com.marutixchange.order_service.dto.*;
import com.marutixchange.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // ✅ CREATE ORDER
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderRequest request) {

        OrderResponse response = orderService.createOrder(request);

        return ResponseEntity.ok(
                new ApiResponse<>("Order created successfully", response)
        );
    }

    // ✅ COMPLETE ORDER
    @PatchMapping("/{orderId}/complete")
    public ResponseEntity<String> completeOrder(@PathVariable Long orderId) {
        orderService.completeOrder(orderId);
        return ResponseEntity.ok("Order marked as PAID");
    }

    // ✅ CANCEL ORDER
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return ResponseEntity.ok("Order cancelled");
    }
}