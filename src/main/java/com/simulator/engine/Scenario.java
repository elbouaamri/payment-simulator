package com.simulator.engine;

/**
 * Simulation outcome scenarios supported by the Rules Engine.
 */
public enum Scenario {

    ACCEPT("00", "Transaction Approved"),
    REFUSE("05", "Do Not Honour"),
    TIMEOUT(null, "Timeout – No Response"),
    TECH_ERROR("96", "System Malfunction"),
    EXPIRED_CARD("54", "Expired Card"),
    INSUFFICIENT_FUNDS("51", "Insufficient Funds");

    private final String defaultResponseCode;
    private final String description;

    Scenario(String defaultResponseCode, String description) {
        this.defaultResponseCode = defaultResponseCode;
        this.description = description;
    }

    public String getDefaultResponseCode() {
        return defaultResponseCode;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Safely parse a scenario name, returning ACCEPT if unknown.
     */
    public static Scenario fromString(String name) {
        if (name == null) return ACCEPT;
        try {
            return valueOf(name.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return ACCEPT;
        }
    }
}
