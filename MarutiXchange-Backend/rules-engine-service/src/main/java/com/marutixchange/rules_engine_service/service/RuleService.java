package com.marutixchange.rules_engine_service.service;

import com.marutixchange.rules_engine_service.model.RuleContext;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * RuleService - Core service that executes Drools rules.
 *
 * Industry flow:
 *   1. Validate input
 *   2. Check buyer-seller conflict (cross-cutting concern)
 *   3. Create Drools KieSession
 *   4. Set agenda group based on rule type
 *   5. Insert RuleContext as fact
 *   6. Fire rules
 *   7. Dispose session (prevent memory leak)
 *   8. Return result
 */
@Service
public class RuleService {

    private static final Logger log = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    private KieContainer kieContainer;

    public RuleContext evaluate(RuleContext context) {

        // 1. Null check
        if (context == null) {
            log.error("Rule evaluation failed: context is null");
            RuleContext err = new RuleContext();
            err.setApproved(false);
            err.setMessage("Invalid request: context is null");
            return err;
        }

        // 2. Type is mandatory
        if (context.getType() == null || context.getType().trim().isEmpty()) {
            log.error("Rule evaluation failed: type is missing");
            context.setApproved(false);
            context.setMessage("Rule type is required (BID, CAR_LISTING, PAYMENT, etc.)");
            return context;
        }

        // 3. Cross-cutting: buyer and seller cannot be same person
        if (context.getUserId() != null
                && context.getSellerId() != null
                && context.getUserId().equals(context.getSellerId())) {
            log.warn("Conflict: buyer and seller are same user={}", context.getUserId());
            context.setApproved(false);
            context.setMessage("Buyer and seller cannot be the same person");
            return context;
        }

        // 4. Validate hour range
        if (context.getHour() != null
                && (context.getHour() < 0 || context.getHour() > 23)) {
            context.setApproved(false);
            context.setMessage("Invalid hour value — must be between 0 and 23");
            return context;
        }

        // 5. Execute Drools rules
        KieSession session = null;
        try {
            session = kieContainer.newKieSession("ksession-rules");

            log.info("Firing rules for type={} | bidAmount={} | currentHighestBid={}",
                    context.getType(), context.getBidAmount(), context.getCurrentHighestBid());

            // Set agenda group to scope which rules fire
            session.getAgenda()
                    .getAgendaGroup(context.getType().toLowerCase())
                    .setFocus();

            session.insert(context);
            int fired = session.fireAllRules();

            log.info("Rules fired={} | approved={} | message={}",
                    fired, context.isApproved(), context.getMessage());

            // Default: if no rule matched, approve
            if (!context.isRuleMatched()) {
                context.setApproved(true);
                context.setMessage("Approved — no rules violated");
                log.info("No rule matched for type={} — default approval applied", context.getType());
            }

        } catch (Exception e) {
            log.error("Rule execution error for type={}: {}", context.getType(), e.getMessage(), e);
            context.setApproved(false);
            context.setMessage("Rule engine error — please try again");
        } finally {
            if (session != null) {
                session.dispose(); // Always dispose to prevent memory leak
            }
        }

        return context;
    }
}
