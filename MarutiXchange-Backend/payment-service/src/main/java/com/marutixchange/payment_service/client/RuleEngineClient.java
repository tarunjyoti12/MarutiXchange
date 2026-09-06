package com.marutixchange.payment_service.client;

import com.marutixchange.payment_service.rules.RuleResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngineClient {

    private final RestTemplate restTemplate;

    @Value("${app.rule-engine.url}")
    private String ruleEngineUrl;

    @Value("${app.rule-engine.enabled:true}")
    private boolean ruleEngineEnabled;

    @Value("${app.rule-engine.retry-attempts:3}")
    private int retryAttempts;

    /**
     * Evaluate rules using external Rule Engine
     */
    public RuleResult evaluate(Object request) {

        if (!ruleEngineEnabled) {
            log.error("Rule Engine is disabled. Please enable it via configuration.");

            RuleResult result = new RuleResult();
            result.addError("RULE_ENGINE_DISABLED", "Rule engine is disabled");
            return result;
        }

        Map<String, Object> ruleRequest = buildRuleRequest(request);
        log.info("Invoking Rule Engine with payload: {}", ruleRequest);

        return executeWithRetry(ruleRequest);
    }

    /**
     * Retry mechanism with exponential backoff
     */
    private RuleResult executeWithRetry(Map<String, Object> request) {

        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                return callRuleEngine(request);
            } catch (RestClientException ex) {

                log.warn("Rule Engine call failed (Attempt {}/{}): {}",
                        attempt, retryAttempts, ex.getMessage());

                if (attempt == retryAttempts) {
                    log.error("Rule Engine unavailable after {} attempts", retryAttempts);

                    RuleResult result = new RuleResult();
                    result.addError("RULE_ENGINE_UNAVAILABLE", "Rule engine service is down");
                    return result;
                }

                try {
                    Thread.sleep(200L * attempt); // exponential backoff
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // fallback (should never reach)
        RuleResult result = new RuleResult();
        result.addError("UNKNOWN_ERROR", "Unexpected failure");
        return result;
    }

    /**
     * Actual REST call
     */
    private RuleResult callRuleEngine(Map<String, Object> request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                ruleEngineUrl,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        log.info("Rule Engine response: {}", body);

        return parseResponse(body);
    }

    /**
     * Build request for Rule Engine
     */
    private Map<String, Object> buildRuleRequest(Object request) {

        Map<String, Object> map = new HashMap<>();

        map.put("type", "payment");
        map.put("price", extractAmount(request));
        map.put("userId", extractUserId(request));
        map.put("sellerId", extractSellerId(request));
        map.put("blacklisted", false);
        map.put("timestamp", System.currentTimeMillis());

        return map;
    }

    /**
     * Parse response from Rule Engine
     */
    private RuleResult parseResponse(Map<String, Object> response) {

        RuleResult result = new RuleResult();

        if (response == null) {
            result.addError("EMPTY_RESPONSE", "Rule engine returned empty response");
            return result;
        }

        Boolean approved = (Boolean) response.get("approved");
        String message = (String) response.get("message");

        if (approved == null || !approved) {
            result.addError("RULE_VALIDATION_FAILED",
                    message != null ? message : "Payment rejected by rule engine");
        }

        return result;
    }

    // =======================
    // Helper Methods
    // =======================

    private double extractAmount(Object request) {
        try {
            return (double) request.getClass()
                    .getMethod("getAmount")
                    .invoke(request);
        } catch (Exception e) {
            log.warn("Unable to extract amount");
            return 0;
        }
    }

    private Long extractUserId(Object request) {
        try {
            return (Long) request.getClass()
                    .getMethod("getBuyerId")
                    .invoke(request);
        } catch (Exception e) {
            return 0L;
        }
    }

    private Long extractSellerId(Object request) {
        try {
            return (Long) request.getClass()
                    .getMethod("getSellerId")
                    .invoke(request);
        } catch (Exception e) {
            return 0L;
        }
    }
}