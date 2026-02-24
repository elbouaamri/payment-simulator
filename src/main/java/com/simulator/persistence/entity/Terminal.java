package com.simulator.persistence.entity;

import jakarta.persistence.*;

/**
 * Terminal known by the simulator.
 */
@Entity
@Table(name = "terminal")
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 16)
    private String terminalId;

    @Column(length = 15)
    private String merchantId;

    @Column(nullable = false)
    private boolean active = true;

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
