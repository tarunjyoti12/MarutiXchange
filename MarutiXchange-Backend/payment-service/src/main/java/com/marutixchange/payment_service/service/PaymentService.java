package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.dto.*;
import java.util.List;

public interface PaymentService {
    PaymentResponse initiatePayment(
            PaymentRequest request);
    PaymentResponse confirmPayment(
            String transactionId);
    PaymentResponse failPayment(
            String transactionId, String reason);
    PaymentResponse getPaymentByTransactionId(
            String transactionId);
    List<PaymentResponse> getBuyerPayments(
            Long buyerId);
    List<PaymentResponse> getSellerPayments(
            Long sellerId);
    EmiCalculatorResponse calculateEmi(
            EmiCalculatorRequest request);
    void checkAndTimeoutPayments();
}