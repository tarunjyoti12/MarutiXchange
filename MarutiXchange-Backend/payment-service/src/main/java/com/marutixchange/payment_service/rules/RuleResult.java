package com.marutixchange.payment_service.rules;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RuleResult {

    private boolean valid = true;

    private final List<RuleViolation> violations = new ArrayList<>();

    private final List<String> warnings = new ArrayList<>();

    private final List<String> suggestions = new ArrayList<>();

    // 🔥 ADD THIS METHOD (MANDATORY)
    public void addViolation(String code, String message, RuleViolation.RuleSeverity severity) {
        violations.add(new RuleViolation(code, message, severity));

        // if critical → mark invalid
        if (severity == RuleViolation.RuleSeverity.CRITICAL) {
            this.valid = false;
        }
    }

    // Optional helper (you can keep or remove)
    public void addError(String code, String message) {
        addViolation(code, message, RuleViolation.RuleSeverity.CRITICAL);
    }

    public void addWarning(String message) {
        warnings.add(message);
    }

    public void addSuggestion(String suggestion) {
        suggestions.add(suggestion);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public List<String> getErrorMessages() {
        return violations.stream()
                .map(RuleViolation::getMessage)
                .collect(Collectors.toList());
    }

    public List<RuleViolation> getViolations() {
        return violations;
    }
}