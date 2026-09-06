package com.marutixchange.order_service.service;

import com.marutixchange.order_service.dto.OrderRequest;
import com.marutixchange.order_service.dto.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(OrderRequest request);

    void completeOrder(Long orderId);   // ✅ added
    void cancelOrder(Long orderId);     // ✅ added
}