package com.simulator.api.dto;

/**
 * Dashboard KPI summary.
 */
public class DashboardDto {

    private long totalTransactions;
    private long successCount;
    private long refusedCount;
    private double successRate;
    private double refusedRate;
    private Double avgLatencyMs;

    // --- Getters & Setters ---
    public long getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(long totalTransactions) { this.totalTransactions = totalTransactions; }

    public long getSuccessCount() { return successCount; }
    public void setSuccessCount(long successCount) { this.successCount = successCount; }

    public long getRefusedCount() { return refusedCount; }
    public void setRefusedCount(long refusedCount) { this.refusedCount = refusedCount; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public double getRefusedRate() { return refusedRate; }
    public void setRefusedRate(double refusedRate) { this.refusedRate = refusedRate; }

    public Double getAvgLatencyMs() { return avgLatencyMs; }
    public void setAvgLatencyMs(Double avgLatencyMs) { this.avgLatencyMs = avgLatencyMs; }
}
