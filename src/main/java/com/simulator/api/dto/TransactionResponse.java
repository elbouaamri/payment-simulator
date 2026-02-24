package com.simulator.api.dto;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response DTO returned after processing a transaction.
 */
public class TransactionResponse {

    private String transactionId;
    private String type;
    private String requestMti;
    private String responseMti;
    private String responseCode39;
    private String scenario;
    private String scenarioDescription;
    private String panMasked;
    private Long amount;
    private String currency;
    private String stan;
    private String rrn;
    private String terminalId;
    private boolean visaLike;
    private String fakeCryptogram;
    private Map<String, String> requestIsoFields;
    private Map<String, String> responseIsoFields;
    private Long durationMs;
    private LocalDateTime createdAt;
    private String correlationId;

    // --- Getters & Setters ---
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getRequestMti() { return requestMti; }
    public void setRequestMti(String requestMti) { this.requestMti = requestMti; }

    public String getResponseMti() { return responseMti; }
    public void setResponseMti(String responseMti) { this.responseMti = responseMti; }

    public String getResponseCode39() { return responseCode39; }
    public void setResponseCode39(String responseCode39) { this.responseCode39 = responseCode39; }

    public String getScenario() { return scenario; }
    public void setScenario(String scenario) { this.scenario = scenario; }

    public String getScenarioDescription() { return scenarioDescription; }
    public void setScenarioDescription(String scenarioDescription) { this.scenarioDescription = scenarioDescription; }

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

    public boolean isVisaLike() { return visaLike; }
    public void setVisaLike(boolean visaLike) { this.visaLike = visaLike; }

    public String getFakeCryptogram() { return fakeCryptogram; }
    public void setFakeCryptogram(String fakeCryptogram) { this.fakeCryptogram = fakeCryptogram; }

    public Map<String, String> getRequestIsoFields() { return requestIsoFields; }
    public void setRequestIsoFields(Map<String, String> requestIsoFields) { this.requestIsoFields = requestIsoFields; }

    public Map<String, String> getResponseIsoFields() { return responseIsoFields; }
    public void setResponseIsoFields(Map<String, String> responseIsoFields) { this.responseIsoFields = responseIsoFields; }

    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
