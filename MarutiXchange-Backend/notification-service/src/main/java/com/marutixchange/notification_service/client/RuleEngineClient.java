package com.marutixchange.notification_service.client;

import com.marutixchange.notification_service.dto.RuleContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class RuleEngineClient {

    private final RestTemplate restTemplate;

    @Value("${app.rule-engine.url}")
    private String ruleEngineUrl;

    public RuleContext evaluate(RuleContext context) {
        return restTemplate.postForObject(
                ruleEngineUrl,
                context,
                RuleContext.class
        );
    }
}