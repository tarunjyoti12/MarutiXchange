package com.marutixchange.order_service.service;

import com.marutixchange.order_service.client.CarListingClient;
import com.marutixchange.order_service.client.NotificationClient;
import com.marutixchange.order_service.client.PaymentClient;
import com.marutixchange.order_service.dto.*;
import com.marutixchange.order_service.entity.*;
import com.marutixchange.order_service.exception.OrderNotFoundException;
import com.marutixchange.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository    orderRepository;
    private final PaymentClient      paymentClient;
    private final CarListingClient   carListingClient;
    private final NotificationClient notificationClient;
    private final KieContainer       kieContainer;

    // ─── Create Order ────────────────────────────────────────────────────────

    @Override
    public OrderResponse createOrder(OrderRequest request) {

        log.info("Creating order for carListingId: {}", request.getCarListingId());

        Order order = new Order();
        order.setBuyerId(request.getBuyerId());
        order.setSellerId(request.getSellerId());
        order.setCarId(request.getCarListingId());
        order.setAmount(request.getAmount());
        order.setStatus(OrderStatus.PAYMENT_PENDING);
        order.setCreatedAt(LocalDateTime.now());

        // Drools validation — fresh session per request (thread-safe)
        List<String> errors = new ArrayList<>();
        KieSession kieSession = kieContainer.newKieSession("defaultKSession");
        try {
            kieSession.setGlobal("errors", errors);
            kieSession.insert(order);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }

        if (!errors.isEmpty()) {
            throw new RuntimeException(errors.toString());
        }

        order = orderRepository.save(order);

        // Call payment-service
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setCarListingId(request.getCarListingId());
        paymentRequest.setBuyerId(request.getBuyerId());
        paymentRequest.setSellerId(request.getSellerId());
        paymentRequest.setAmount(request.getAmount());
        paymentRequest.setPaymentMethod(request.getPaymentMethod());

        PaymentResponse paymentResponse = paymentClient.createPayment(paymentRequest);

        String paymentId = paymentResponse.getPaymentId();
        if (paymentId == null) {
            paymentId = paymentResponse.getTransactionId();
        }

        order.setPaymentId(paymentId);
        orderRepository.save(order);

        // Notify buyer order is created
        sendNotification(
                request.getBuyerId(),
                "ORDER_CREATED",
                "Order Created",
                "Your order has been created. Complete payment to confirm."
        );

        log.info("Order created: id={}, paymentId={}", order.getId(), paymentId);

        return new OrderResponse(order.getId(), order.getStatus().name(), order.getPaymentId());
    }

    // ─── Complete Order (called after payment success) ────────────────────────

    @Override
    public void completeOrder(Long orderId) {

        log.info("Completing order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // Mark car listing as SOLD in car-listing-service
        try {
            carListingClient.markAsSold(order.getCarId());
            log.info("Car listing {} marked as SOLD", order.getCarId());
        } catch (Exception e) {
            log.error("Failed to mark listing as sold: {}", e.getMessage());
            // Don't fail order completion if car-listing call fails
        }

        // Notify buyer
        sendNotification(
                order.getBuyerId(),
                "ORDER_CONFIRMED",
                "Order Confirmed!",
                "Your payment is confirmed. The car is now reserved for you."
        );

        // Notify seller
        sendNotification(
                order.getSellerId(),
                "ORDER_CONFIRMED",
                "Your car has been sold!",
                "A buyer has completed payment for your listing."
        );

        log.info("Order {} marked as PAID", orderId);
    }

    // ─── Cancel Order ────────────────────────────────────────────────────────

    @Override
    public void cancelOrder(Long orderId) {

        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + orderId));

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Notify buyer
        sendNotification(
                order.getBuyerId(),
                "ORDER_CANCELLED",
                "Order Cancelled",
                "Your order has been cancelled."
        );

        log.info("Order {} cancelled", orderId);
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private void sendNotification(Long userId, String type, String title, String body) {
        try {
            notificationClient.sendNotification(
                    NotificationRequest.builder()
                            .userId(userId)
                            .notificationType(type)
                            .channel("IN_APP")
                            .title(title)
                            .body(body)
                            .build()
            );
        } catch (Exception e) {
            log.error("Notification failed for userId={}: {}", userId, e.getMessage());
            // Never fail business logic due to notification failure
        }
    }
}
