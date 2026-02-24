package com.simulator.engine;

import com.simulator.persistence.entity.SimulationProfile;
import com.simulator.persistence.entity.SimulationRule;
import com.simulator.persistence.repository.SimulationRuleRepository;
import com.simulator.persistence.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RulesEngineServiceTest {

    @Mock
    private SimulationRuleRepository ruleRepository;

    @Mock
    private TerminalRepository terminalRepository;

    private RulesEngineService engine;

    @BeforeEach
    void setUp() {
        engine = new RulesEngineService(ruleRepository, terminalRepository);
    }

    private SimulationRule rule(String name, int priority, String condType, String condValue, String outcome, String rc) {
        SimulationRule r = new SimulationRule();
        r.setName(name);
        r.setPriority(priority);
        r.setEnabled(true);
        r.setConditionType(condType);
        r.setConditionValue(condValue);
        r.setOutcomeScenario(outcome);
        r.setResponseCode39(rc);
        return r;
    }

    @Test
    @DisplayName("Expired card → EXPIRED_CARD scenario, RC 54")
    void testExpiredCard() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                rule("Expired", 1, "EXPIRED", "", "EXPIRED_CARD", "54"),
                rule("Accept", 10, "ALWAYS", "", "ACCEPT", "00")
        ));

        // Expiry 2301 = Jan 2023 → expired
        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2301", 10000, "TERM0001", null);
        assertEquals(Scenario.EXPIRED_CARD, result.getScenario());
        assertEquals("54", result.getResponseCode39());
    }

    @Test
    @DisplayName("Valid card → ACCEPT scenario, RC 00")
    void testAccept() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                rule("Expired", 1, "EXPIRED", "", "EXPIRED_CARD", "54"),
                rule("Accept", 10, "ALWAYS", "", "ACCEPT", "00")
        ));

        // Expiry 2812 = Dec 2028 → valid
        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 10000, "TERM0001", null);
        assertEquals(Scenario.ACCEPT, result.getScenario());
        assertEquals("00", result.getResponseCode39());
    }

    @Test
    @DisplayName("Amount exceeds limit → INSUFFICIENT_FUNDS, RC 51")
    void testInsufficientFunds() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                rule("Amount", 2, "AMOUNT_GREATER_THAN", "50000", "INSUFFICIENT_FUNDS", "51"),
                rule("Accept", 10, "ALWAYS", "", "ACCEPT", "00")
        ));

        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 60000, "TERM0001", null);
        assertEquals(Scenario.INSUFFICIENT_FUNDS, result.getScenario());
        assertEquals("51", result.getResponseCode39());
    }

    @Test
    @DisplayName("Unknown terminal → REFUSE, RC 05")
    void testUnknownTerminal() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                rule("Unknown Term", 3, "TERMINAL_UNKNOWN", "", "REFUSE", "05"),
                rule("Accept", 10, "ALWAYS", "", "ACCEPT", "00")
        ));
        when(terminalRepository.existsByTerminalId("UNKNOWN")).thenReturn(false);

        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 10000, "UNKNOWN", null);
        assertEquals(Scenario.REFUSE, result.getScenario());
        assertEquals("05", result.getResponseCode39());
    }

    @Test
    @DisplayName("PAN prefix match → rule fires")
    void testPanPrefix() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of(
                rule("Visa PAN", 4, "PAN_PREFIX", "4", "ACCEPT", "00")
        ));

        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 10000, "TERM0001", null);
        assertEquals(Scenario.ACCEPT, result.getScenario());
        assertEquals("Visa PAN", result.getMatchedRuleName());
    }

    @Test
    @DisplayName("Profile forces TIMEOUT scenario")
    void testProfileForcesTimeout() {
        SimulationProfile profile = new SimulationProfile();
        profile.setName("TIMEOUT_PROFILE");
        profile.setDefaultScenario("TIMEOUT");

        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 10000, "TERM0001", profile);
        assertEquals(Scenario.TIMEOUT, result.getScenario());
    }

    @Test
    @DisplayName("Profile forces TECH_ERROR → RC 96")
    void testProfileForcesTechError() {
        SimulationProfile profile = new SimulationProfile();
        profile.setName("ERROR_PROFILE");
        profile.setDefaultScenario("TECH_ERROR");

        RuleEvaluationResult result = engine.evaluate("4111111111111111", "2812", 10000, "TERM0001", profile);
        assertEquals(Scenario.TECH_ERROR, result.getScenario());
        assertEquals("96", result.getResponseCode39());
    }

    @Test
    @DisplayName("No rules match → default ACCEPT")
    void testDefaultAccept() {
        when(ruleRepository.findByEnabledTrueOrderByPriorityAsc()).thenReturn(List.of());

        RuleEvaluationResult result = engine.evaluate("5111111111111111", "2812", 10000, "TERM0001", null);
        assertEquals(Scenario.ACCEPT, result.getScenario());
        assertEquals("00", result.getResponseCode39());
    }
}
