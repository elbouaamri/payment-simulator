package com.simulator.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Logs every ISO 8583 transaction processed by the simulator.
 */
@Entity
@Table(name = "transaction_log")
public class TransactionLog {

    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 20)
    private String type; // AUTHORIZE, REFUND, CANCEL, REVERSAL

    @Column(nullable = false)
    private boolean visaLike;

    @Column(length = 4)
    private String requestMti;

    @Column(length = 4)
    private String responseMti;

    @Column(length = 19)
    private String panMasked;

    @Column(nullable = false)
    private Long amount;

    @Column(length = 3)
    private String currency;

    @Column(length = 12)
    private String stan;

    @Column(length = 12)
    private String rrn;

    @Column(length = 16)
    private String terminalId;

    @Column(length = 2)
    private String responseCode39;

    @Column(length = 30)
    private String scenario;

    @Column(length = 5000)
    private String requestIsoFields;

    @Column(length = 5000)
    private String responseIsoFields;

    @Column(columnDefinition = "TEXT")
    private String requestIsoRaw;

    @Column(columnDefinition = "TEXT")
    private String responseIsoRaw;

    private Long durationMs;

    @Column(length = 36)
    private String correlationId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isVisaLike() { return visaLike; }
    public void setVisaLike(boolean visaLike) { this.visaLike = visaLike; }

    public String getRequestMti() { return requestMti; }
    public void setRequestMti(String requestMti) { this.requestMti = requestMti; }

    public String getResponseMti() { return responseMti; }
    public void setResponseMti(String responseMti) { this.responseMti = responseMti; }

    public String getPanMasked() { return panMasked; }
    public void setPanMasked(String panMasked) { this.panMasked = panMasked; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStan() { return stan; }
    public void setStan(String stan) { this.stan = stan; }

    public String getRrn() { return rrn; }
    public void setRrn(String rrn) { this.rrn = rrn; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getResponseCode39() { return responseCode39; }
    public void setResponseCode39(String responseCode39) { this.responseCode39 = responseCode39; }

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public String getRequestIsoFields() { return requestIsoFields; }
    public void setRequestIsoFields(String requestIsoFields) { this.requestIsoFields = requestIsoFields; }

    public String getResponseIsoFields() { return responseIsoFields; }
    public void setResponseIsoFields(String responseIsoFields) { this.responseIsoFields = responseIsoFields; }

    public String getRequestIsoRaw() { return requestIsoRaw; }
    public void setRequestIsoRaw(String requestIsoRaw) { this.requestIsoRaw = requestIsoRaw; }

    public String getResponseIsoRaw() { return responseIsoRaw; }
    public void setResponseIsoRaw(String responseIsoRaw) { this.responseIsoRaw = responseIsoRaw; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
