package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.client.RuleEngineClient;
import com.marutixchange.payment_service.client.NotificationClient;
import com.marutixchange.payment_service.dto.*;
import com.marutixchange.payment_service.entity.Payment;
import com.marutixchange.payment_service.exception.*;
import com.marutixchange.payment_service.repository.PaymentRepository;
import com.marutixchange.payment_service.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RuleEngineClient ruleEngineClient;
    private final EscrowService escrowService;
    private final NotificationClient notificationClient;

    @Value("${app.payment.timeout.minutes}")
    private int timeoutMinutes;

    @Value("${app.payment.token.amount}")
    private double tokenAmount;

    @Override
    @Transactional
    public PaymentResponse initiatePayment(PaymentRequest request) {

        log.info("Initiating payment for buyer: {}, amount: {}",
                request.getBuyerId(), request.getAmount());

        if (request.getIdempotencyKey() != null) {
            Optional<Payment> existing =
                    paymentRepository.findByIdempotencyKey(
                            request.getIdempotencyKey());

            if (existing.isPresent()) {
                return PaymentResponse.fromEntity(existing.get());
            }
        }

        RuleResult ruleResult = ruleEngineClient.evaluate(request);

        log.info("Rule Engine Response: {}", ruleResult);

        if (!ruleResult.isValid()) {
            log.error("Payment rejected by rule engine: {}", ruleResult.getErrorMessages());

            throw new PaymentValidationException(
                    String.join(", ", ruleResult.getErrorMessages())
            );
        }

        if (ruleResult.hasWarnings()) {
            log.warn("Payment warnings: {}", ruleResult.getWarnings());
        }

        String txnId = "MM" + System.currentTimeMillis()
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        String invoiceNo = "INV-MX-" + System.currentTimeMillis();

        boolean isToken = Boolean.TRUE.equals(request.getIsTokenPayment());

        Payment.PaymentMethod paymentMethod =
                Payment.PaymentMethod.valueOf(
                        request.getPaymentMethod().toUpperCase().replace(" ", "_"));

        Payment.PaymentType paymentType =
                request.getPaymentType() != null
                        ? Payment.PaymentType.valueOf(
                        request.getPaymentType().toUpperCase().replace(" ", "_"))
                        : Payment.PaymentType.TOKEN;

        Payment payment = Payment.builder()
                .transactionId(txnId)
                .idempotencyKey(request.getIdempotencyKey())
                .carListingId(request.getCarListingId())
                .auctionId(request.getAuctionId())
                .buyerId(request.getBuyerId())
                .sellerId(request.getSellerId())
                .amount(request.getAmount())
                .tokenAmount(tokenAmount)
                .remainingAmount(isToken ? request.getAmount() - tokenAmount : 0.0)
                .paymentMethod(paymentMethod)
                .paymentType(paymentType)
                .upiId(request.getUpiId())
                .upiApp(request.getUpiApp())
                .bankName(request.getBankName())
                .invoiceNumber(invoiceNo)
                .isTokenPayment(isToken)
                .status(Payment.PaymentStatus.PENDING)
                .timeoutAt(LocalDateTime.now().plusMinutes(timeoutMinutes))
                .build();

        Payment saved = paymentRepository.save(payment);

        log.info("Payment initiated successfully: {}", saved.getTransactionId());

        return PaymentResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String transactionId) {

        Payment payment = getPaymentEntity(transactionId);

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new PaymentAlreadyDoneException("Payment already confirmed");
        }

        if (payment.getStatus() == Payment.PaymentStatus.TIMEOUT) {
            throw new PaymentValidationException("Payment timed out");
        }

        // 🔥 RULE ENGINE CALL (ADDED)
        RuleEngineRequest ruleRequest = new RuleEngineRequest();
        ruleRequest.setType("PAYMENT");
        ruleRequest.setUserId(payment.getBuyerId());
        ruleRequest.setSellerId(payment.getSellerId());
        ruleRequest.setPrice(payment.getAmount());

        RuleResult ruleResult = ruleEngineClient.evaluate(ruleRequest);

        log.info("Rule Engine Response in confirmPayment: {}", ruleResult);

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTimeoutAt(null);

        Payment saved = paymentRepository.save(payment);

        // 🔥 ESCROW LOGIC (ADDED & FIXED)
        if (!ruleResult.getErrorMessages().isEmpty()) {

            log.info("Escrow triggered");

            EscrowRequest escrowRequest = new EscrowRequest();
            escrowRequest.setPaymentId(saved.getId());
            escrowRequest.setCarListingId(saved.getCarListingId());
            escrowRequest.setBuyerId(saved.getBuyerId());
            escrowRequest.setSellerId(saved.getSellerId());
            escrowRequest.setHeldAmount(saved.getAmount());

            escrowService.createEscrow(escrowRequest);
        }

        // 🔔 NOTIFICATION (UNCHANGED)
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(saved.getBuyerId());
            notification.setChannel("IN_APP");
            notification.setNotificationType("PAYMENT_SUCCESS");
            notification.setTitle("Payment Successful");
            notification.setBody("Your payment was completed successfully!");

            notificationClient.sendNotification(notification);

            log.info("✅ Payment notification sent");
        } catch (Exception e) {
            log.error("❌ Payment notification failed: {}", e.getMessage());
        }

        return PaymentResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public PaymentResponse failPayment(String transactionId, String reason) {

        Payment payment = getPaymentEntity(transactionId);

        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setFailureReason(reason);

        Payment saved = paymentRepository.save(payment);

        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setUserId(saved.getBuyerId());
            notification.setChannel("IN_APP");
            notification.setNotificationType("PAYMENT_FAILED");
            notification.setTitle("Payment Failed");
            notification.setBody("Your payment has failed. Please try again.");

            notificationClient.sendNotification(notification);

            log.info("⚠️ Payment failure notification sent");
        } catch (Exception e) {
            log.error("❌ Payment failure notification failed: {}", e.getMessage());
        }

        return PaymentResponse.fromEntity(saved);
    }

    @Override
    public PaymentResponse getPaymentByTransactionId(String transactionId) {
        return PaymentResponse.fromEntity(getPaymentEntity(transactionId));
    }

    @Override
    public List<PaymentResponse> getBuyerPayments(Long buyerId) {
        return paymentRepository.findByBuyerId(buyerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<PaymentResponse> getSellerPayments(Long sellerId) {
        return paymentRepository.findBySellerId(sellerId)
                .stream()
                .map(PaymentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public EmiCalculatorResponse calculateEmi(EmiCalculatorRequest request) {

        double principal = request.getLoanAmount();
        double rate = request.getAnnualInterestRate() / 12 / 100;
        int tenure = request.getTenureMonths();

        double emi;

        if (rate == 0) {
            emi = principal / tenure;
        } else {
            double power = Math.pow(1 + rate, tenure);
            emi = principal * rate * power / (power - 1);
        }

        double roundedEmi = Math.round(emi * 100.0) / 100.0;

        double totalPayment = roundedEmi * tenure;
        double totalInterest = totalPayment - principal;

        return EmiCalculatorResponse.builder()
                .loanAmount(principal)
                .annualInterestRate(request.getAnnualInterestRate())
                .tenureMonths(tenure)
                .monthlyEmi(roundedEmi)
                .totalPayable(Math.round(totalPayment * 100.0) / 100.0)
                .totalInterest(Math.round(totalInterest * 100.0) / 100.0)
                .build();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndTimeoutPayments() {

        List<Payment> timedOut =
                paymentRepository.findTimedOutPayments(LocalDateTime.now());

        timedOut.forEach(p -> {
            p.setStatus(Payment.PaymentStatus.TIMEOUT);
            p.setFailureReason("Payment timed out");
            paymentRepository.save(p);
        });
    }

    private Payment getPaymentEntity(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException(transactionId));
    }
}