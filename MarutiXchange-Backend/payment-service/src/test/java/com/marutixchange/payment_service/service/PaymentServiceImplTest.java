package com.marutixchange.payment_service.service;

import com.marutixchange.payment_service.client.RuleEngineClient;
import com.marutixchange.payment_service.dto.EmiCalculatorRequest;
import com.marutixchange.payment_service.dto.EmiCalculatorResponse;
import com.marutixchange.payment_service.dto.PaymentRequest;
import com.marutixchange.payment_service.dto.PaymentResponse;
import com.marutixchange.payment_service.entity.Payment;
import com.marutixchange.payment_service.exception.PaymentValidationException;
import com.marutixchange.payment_service.repository.PaymentRepository;
import com.marutixchange.payment_service.rules.RuleResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository paymentRepository;

    // ✅ UPDATED (Drools → RuleEngineClient)
    @Mock
    private RuleEngineClient ruleEngineClient;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                paymentService, "timeoutMinutes", 30);
        ReflectionTestUtils.setField(
                paymentService, "tokenAmount", 10000.0);

        paymentRequest = PaymentRequest.builder()
                .carListingId(1L)
                .buyerId(3L)
                .sellerId(2L)
                .amount(10000.0)
                .paymentMethod("UPI")
                .paymentType("TOKEN")
                .upiId("buyer@gpay")
                .isTokenPayment(true)
                .build();
    }

    @Test
    void initiatePayment_success() {

        // ✅ Rule engine returns VALID result
        RuleResult validResult = new RuleResult();

        when(ruleEngineClient.evaluate(any()))
                .thenReturn(validResult);

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentResponse response =
                paymentService.initiatePayment(paymentRequest);

        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals("UPI", response.getPaymentMethod());

        verify(paymentRepository, times(1))
                .save(any(Payment.class));
    }

    @Test
    void initiatePayment_fails_ruleViolation() {

        RuleResult invalidResult = new RuleResult();
        invalidResult.addError(
                "BUYER_SELLER_DIFFERENT",
                "Buyer and seller cannot be the same person");

        when(ruleEngineClient.evaluate(any()))
                .thenReturn(invalidResult);

        assertThrows(
                PaymentValidationException.class,
                () -> paymentService.initiatePayment(paymentRequest)
        );
    }

    @Test
    void calculateEmi_correctValues() {

        EmiCalculatorRequest request = new EmiCalculatorRequest();
        request.setLoanAmount(700000.0);
        request.setAnnualInterestRate(8.5);
        request.setTenureMonths(36);

        EmiCalculatorResponse response =
                paymentService.calculateEmi(request);

        assertNotNull(response);
        assertNotNull(response.getMonthlyEmi());
        assertTrue(response.getMonthlyEmi() > 0);
        assertTrue(response.getTotalInterest() > 0);
        assertTrue(response.getTotalPayable() > response.getLoanAmount());
    }
}