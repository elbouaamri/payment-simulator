package com.simulator.persistence.entity;

import jakarta.persistence.*;

/**
 * Simulation profile – determines behaviour (latency, scenario, amount limit).
 */
@Entity
@Table(name = "simulation_profile")
public class SimulationProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false, length = 30)
    private String defaultScenario; // ACCEPT, REFUSE, TIMEOUT, TECH_ERROR, EXPIRED_CARD, INSUFFICIENT_FUNDS

    @Column(nullable = false)
    private Integer latencyMs = 0;

    private Long amountLimit;

    @Column(nullable = false)
    private boolean active = true;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefaultScenario() { return defaultScenario; }
    public void setDefaultScenario(String defaultScenario) { this.defaultScenario = defaultScenario; }

    public Integer getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Integer latencyMs) { this.latencyMs = latencyMs; }

    public Long getAmountLimit() { return amountLimit; }
    public void setAmountLimit(Long amountLimit) { this.amountLimit = amountLimit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
