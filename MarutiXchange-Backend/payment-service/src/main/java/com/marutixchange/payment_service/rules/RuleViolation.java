package com.marutixchange.payment_service.rules;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RuleViolation {

    private String code;
    private String message;
    private RuleSeverity severity;

    // 🔥 ADD THIS ENUM (MANDATORY)
    public enum RuleSeverity {
        CRITICAL,
        ERROR,
        WARNING
    }
}