package com.simulator.engine;

import com.simulator.persistence.entity.SimulationProfile;
import com.simulator.persistence.entity.SimulationRule;
import com.simulator.persistence.repository.SimulationRuleRepository;
import com.simulator.persistence.repository.TerminalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Rules Engine – evaluates simulation rules in priority order to determine the
 * transaction outcome (Scenario + Response Code 39).
 *
 * <p>Rules are loaded from the database, sorted by priority ascending (lower = higher priority).
 * The first matching rule wins.</p>
 */
@Service
public class RulesEngineService {

    private static final Logger log = LoggerFactory.getLogger(RulesEngineService.class);

    private final SimulationRuleRepository ruleRepository;
    private final TerminalRepository terminalRepository;

    public RulesEngineService(SimulationRuleRepository ruleRepository,
                              TerminalRepository terminalRepository) {
        this.ruleRepository = ruleRepository;
        this.terminalRepository = terminalRepository;
    }

    /**
     * Evaluate rules against the transaction parameters.
     *
     * @param pan         Card PAN
     * @param expiry      Card expiry (YYMM or MMYY)
     * @param amount      Transaction amount in minor units
     * @param terminalId  Terminal identifier
     * @param profile     Active simulation profile (may override with default scenario)
     * @return the first matching rule result, or default ACCEPT
     */
    public RuleEvaluationResult evaluate(String pan, String expiry, long amount,
                                         String terminalId, SimulationProfile profile) {

        // If profile forces a specific scenario (not ACCEPT), use it directly
        if (profile != null && profile.getDefaultScenario() != null) {
            Scenario profileScenario = Scenario.fromString(profile.getDefaultScenario());
            if (profileScenario != Scenario.ACCEPT) {
                log.info("Profile '{}' forces scenario: {}", profile.getName(), profileScenario);
                return new RuleEvaluationResult(profileScenario,
                        profileScenario.getDefaultResponseCode(),
                        "Profile: " + profile.getName());
            }
        }

        // Evaluate rules from DB, sorted by priority
        List<SimulationRule> rules = ruleRepository.findByEnabledTrueOrderByPriorityAsc();

        for (SimulationRule rule : rules) {
            if (matches(rule, pan, expiry, amount, terminalId, profile)) {
                Scenario scenario = Scenario.fromString(rule.getOutcomeScenario());
                String rc = rule.getResponseCode39() != null ? rule.getResponseCode39() : scenario.getDefaultResponseCode();
                log.info("Rule '{}' matched → scenario={}, rc39={}", rule.getName(), scenario, rc);
                return new RuleEvaluationResult(scenario, rc, rule.getName());
            }
        }

        log.info("No rule matched, defaulting to ACCEPT");
        return RuleEvaluationResult.accept();
    }

    /**
     * Check if a single rule matches the transaction parameters.
     */
    private boolean matches(SimulationRule rule, String pan, String expiry,
                            long amount, String terminalId, SimulationProfile profile) {
        String type = rule.getConditionType();
        String value = rule.getConditionValue();

        return switch (type) {
            case "PAN_PREFIX" -> pan != null && value != null && pan.startsWith(value);

            case "EXPIRED" -> isExpired(expiry);

            case "AMOUNT_GREATER_THAN" -> {
                long limit = parseLong(value, Long.MAX_VALUE);
                // Also check profile amount limit
                if (profile != null && profile.getAmountLimit() != null) {
                    limit = Math.min(limit, profile.getAmountLimit());
                }
                yield amount > limit;
            }

            case "TERMINAL_UNKNOWN" -> terminalId != null && !terminalRepository.existsByTerminalId(terminalId);

            case "ALWAYS" -> true;

            default -> {
                log.warn("Unknown condition type: {}", type);
                yield false;
            }
        };
    }

    /**
     * Check if card expiry is in the past. Accepts YYMM or MMYY format.
     */
    private boolean isExpired(String expiry) {
        if (expiry == null || expiry.length() != 4) return false;
        try {
            // Try YYMM (standard ISO 8583 field 14)
            int yy = Integer.parseInt(expiry.substring(0, 2));
            int mm = Integer.parseInt(expiry.substring(2, 4));
            if (mm < 1 || mm > 12) {
                // Try MMYY
                mm = Integer.parseInt(expiry.substring(0, 2));
                yy = Integer.parseInt(expiry.substring(2, 4));
            }
            int year = 2000 + yy;
            YearMonth cardExpiry = YearMonth.of(year, mm);
            return cardExpiry.isBefore(YearMonth.now());
        } catch (Exception e) {
            log.warn("Cannot parse expiry '{}': {}", expiry, e.getMessage());
            return false;
        }
    }

    private long parseLong(String s, long defaultVal) {
        try {
            return Long.parseLong(s);
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
