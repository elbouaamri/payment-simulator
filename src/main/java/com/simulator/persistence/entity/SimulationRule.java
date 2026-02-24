package com.simulator.persistence.entity;

import jakarta.persistence.*;

/**
 * Rule evaluated by the Rules Engine to determine transaction outcome.
 */
@Entity
@Table(name = "simulation_rule")
public class SimulationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private Integer priority; // lower = higher priority

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false, length = 30)
    private String conditionType; // PAN_PREFIX, AMOUNT_GREATER_THAN, TERMINAL_UNKNOWN, EXPIRED, ALWAYS

    @Column(length = 100)
    private String conditionValue;

    @Column(nullable = false, length = 30)
    private String outcomeScenario; // ACCEPT, REFUSE, TIMEOUT, TECH_ERROR, EXPIRED_CARD, INSUFFICIENT_FUNDS

    @Column(nullable = false, length = 2)
    private String responseCode39;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public String getConditionType() { return conditionType; }
    public void setConditionType(String conditionType) { this.conditionType = conditionType; }

    public String getConditionValue() { return conditionValue; }
    public void setConditionValue(String conditionValue) { this.conditionValue = conditionValue; }

    public String getOutcomeScenario() { return outcomeScenario; }
    public void setOutcomeScenario(String outcomeScenario) { this.outcomeScenario = outcomeScenario; }

    public String getResponseCode39() { return responseCode39; }
    public void setResponseCode39(String responseCode39) { this.responseCode39 = responseCode39; }
}
