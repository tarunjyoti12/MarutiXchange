package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.client.RuleEngineClient;
import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.entity.Payment;
import com.marutixchange.payment_service.entity.Refund;
import com.marutixchange.payment_service.exception.*;
import com.marutixchange.payment_service.repository.PaymentRepository;
import com.marutixchange.payment_service.repository.RefundRepository;
import com.marutixchange.payment_service.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefundServiceImpl implements RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RuleEngineClient ruleEngineClient;

    @Value("${app.payment.refund.cancellation-fee-percent}")
    private double cancellationFeePercent;

    @Override
    @Transactional
    public RefundResponse initiateRefund(RefundRequest request) {

        if (refundRepository.existsByTransactionId(request.getTransactionId())) {
            throw new PaymentAlreadyDoneException("Refund already initiated");
        }

        Payment payment = paymentRepository
                .findByTransactionId(request.getTransactionId())
                .orElseThrow(() -> new PaymentNotFoundException(request.getTransactionId()));

        RuleResult result = ruleEngineClient.evaluate(request);

        if (!result.isValid()) {
            throw new RefundNotEligibleException(
                    String.join(", ", result.getErrorMessages())
            );
        }

        double refundAmount = payment.getAmount();

        String ref = "REF-" + UUID.randomUUID();

        Refund refund = Refund.builder()
                .refundReference(ref)
                .transactionId(request.getTransactionId())
                .paymentId(payment.getId())
                .buyerId(request.getBuyerId())
                .originalAmount(payment.getAmount())
                .refundAmount(refundAmount)
                .status(Refund.RefundStatus.PROCESSING)
                .processedAt(LocalDateTime.now())
                .build();

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        return RefundResponse.fromEntity(refundRepository.save(refund));
    }

    @Override
    public RefundResponse getRefundByReference(String refundReference) {
        return null;
    }

    @Override
    public List<RefundResponse> getBuyerRefunds(Long buyerId) {
        return refundRepository.findByBuyerId(buyerId)
                .stream()
                .map(RefundResponse::fromEntity)
                .collect(Collectors.toList());
    }
}