package com.marutixchange.rules_engine_service.controller;

import com.marutixchange.rules_engine_service.model.RuleContext;
import com.marutixchange.rules_engine_service.service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * RuleController - Single endpoint that evaluates any business rule.
 *
 * Industry pattern: One universal evaluation endpoint.
 * The rule type is determined by RuleContext.type field.
 *
 * Called internally by:
 *   - bidding-service (bid validation)
 *   - payment-service (payment validation)
 *   - notification-service (notification rules)
 *   - order-service (order validation)
 *   - car-listing-service (listing validation)
 */
@RestController
@RequestMapping("/rules")
@CrossOrigin(origins = "*")
public class RuleController {

    @Autowired
    private RuleService ruleService;

    /**
     * POST /rules/evaluate
     * Evaluates business rules for the given context.
     *
     * @param context RuleContext with type and relevant fields
     * @return RuleContext with approved=true/false and message
     */
    @PostMapping("/evaluate")
    public ResponseEntity<RuleContext> evaluate(@RequestBody RuleContext context) {
        RuleContext result = ruleService.evaluate(context);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /rules/health
     * Health check for the rules engine service.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Rules Engine is UP");
    }
}
