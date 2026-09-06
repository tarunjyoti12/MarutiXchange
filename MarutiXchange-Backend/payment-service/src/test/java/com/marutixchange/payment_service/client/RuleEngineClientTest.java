package com.marutixchange.payment_service.client;

import com.marutixchange.payment_service.dto.PaymentRequest;
import com.marutixchange.payment_service.rules.RuleResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@DisplayName("RuleEngineClient Tests - Fixed")
class RuleEngineClientTest {

    @Mock
    private RestTemplate restTemplate;

    private RuleEngineClient ruleEngineClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ruleEngineClient = new RuleEngineClient(restTemplate);

        ReflectionTestUtils.setField(ruleEngineClient, "ruleEngineUrl", "http://localhost:8069/api/rules/evaluate");
        ReflectionTestUtils.setField(ruleEngineClient, "ruleEngineEnabled", true);
        ReflectionTestUtils.setField(ruleEngineClient, "retryAttempts", 3);
    }

    private PaymentRequest buildRequest() {
        return PaymentRequest.builder()
                .buyerId(1L)
                .sellerId(2L)
                .amount(50000.0)
                .carListingId(1L)
                .auctionId(1L)
                .paymentMethod("UPI")
                .build();
    }

    @Test
    @DisplayName("Should approve valid payment")
    void testEvaluatePaymentApproved() {

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("approved", true);
        responseMap.put("message", "Payment approved");

        ResponseEntity<Map> response = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertTrue(result.isValid());
    }

    @Test
    @DisplayName("Should reject payment when rule engine returns approved=false")
    void testEvaluatePaymentRejected() {

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("approved", false);
        responseMap.put("message", "Payment rejected");

        ResponseEntity<Map> response = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertFalse(result.isValid());
        assertFalse(result.getErrorMessages().isEmpty());
    }

    @Test
    @DisplayName("Should handle rule engine unavailable")
    void testEvaluatePaymentWithRetryFailure() {

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new RestClientException("Connection refused"));

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertFalse(result.isValid());
        assertFalse(result.getErrorMessages().isEmpty());
    }

    @Test
    @DisplayName("Should return error when rule engine is disabled")
    void testEvaluatePaymentWithRuleEngineDisabled() {

        ReflectionTestUtils.setField(ruleEngineClient, "ruleEngineEnabled", false);

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should handle null response safely")
    void testEvaluatePaymentWithNullResponse() {

        ResponseEntity<Map> response = new ResponseEntity<>(null, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertFalse(result.isValid());
    }

    @Test
    @DisplayName("Should handle response with warnings")
    void testEvaluatePaymentWithWarnings() {

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("approved", true);
        responseMap.put("message", "Payment approved with warnings");

        ResponseEntity<Map> response = new ResponseEntity<>(responseMap, HttpStatus.OK);

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(response);

        RuleResult result = ruleEngineClient.evaluate(buildRequest());

        assertTrue(result.isValid());
    }
}