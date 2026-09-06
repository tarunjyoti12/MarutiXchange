package com.marutixchange.order_service.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long buyerId;
    private Long sellerId;
    private Long carId;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String paymentId;

    private LocalDateTime createdAt;
}