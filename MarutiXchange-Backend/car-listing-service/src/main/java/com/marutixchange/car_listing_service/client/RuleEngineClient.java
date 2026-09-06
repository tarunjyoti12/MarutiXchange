package com.marutixchange.car_listing_service.client;

import com.marutixchange.car_listing_service.dto.RuleContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class RuleEngineClient {

    private final RestTemplate restTemplate;

    @Value("${app.rule-engine.url}")
    private String ruleEngineUrl;

    @Value("${app.rule-engine.enabled:true}")
    private boolean enabled;

    public RuleContext evaluateRules(RuleContext context) {

        if (!enabled) {
            return context;
        }

        try {
            return restTemplate.postForObject(
                    ruleEngineUrl,
                    context,
                    RuleContext.class
            );
        } catch (Exception e) {
            return null;
        }
    }
}