package com.marutixchange.bidding_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEngineClient {

    private final RestTemplate restTemplate;

    @Value("${app.rule-engine.url}")
    private String ruleEngineUrl;

    public Map<String, Object> evaluateRules(Map<String, Object> request) {
        try {
            log.info("🚀 Calling Rule Engine: {}", request);

            Map<String, Object> response = restTemplate.postForObject(
                    ruleEngineUrl,
                    request,
                    Map.class
            );

            log.info("✅ Rule Engine Response: {}", response);

            return response;

        } catch (Exception e) {
            log.error("❌ Rule Engine call failed", e);
            throw new RuntimeException("Rule Engine service unavailable");
        }
    }
}