package com.simulator.engine;

/**
 * Result returned by the Rules Engine containing the scenario, response code, and matched rule name.
 */
public class RuleEvaluationResult {

    private final Scenario scenario;
    private final String responseCode39;
    private final String matchedRuleName;

    public RuleEvaluationResult(Scenario scenario, String responseCode39, String matchedRuleName) {
        this.scenario = scenario;
        this.responseCode39 = responseCode39;
        this.matchedRuleName = matchedRuleName;
    }

    public Scenario getScenario() { return scenario; }
    public String getResponseCode39() { return responseCode39; }
    public String getMatchedRuleName() { return matchedRuleName; }

    public static RuleEvaluationResult accept() {
        return new RuleEvaluationResult(Scenario.ACCEPT, "00", "Default Accept");
    }

    @Override
    public String toString() {
        return "RuleEvaluationResult{scenario=" + scenario + ", rc39=" + responseCode39 + ", rule=" + matchedRuleName + '}';
    }
}
