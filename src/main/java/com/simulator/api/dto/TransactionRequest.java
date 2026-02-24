package com.simulator.api.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for initiating a transaction.
 */
public class TransactionRequest {

    @NotBlank(message = "Transaction type is required")
    private String type; // AUTHORIZE, REFUND, CANCEL, REVERSAL

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency = "504"; // MAD

    @NotBlank(message = "Terminal ID is required")
    private String terminalId;

    @NotBlank(message = "PAN is required")
    @Size(min = 13, max = 19, message = "PAN must be 13-19 digits")
    private String pan;

    @Size(min = 4, max = 4, message = "Expiry must be 4 digits (YYMM)")
    private String expiry;

    private String stan;
    private String rrn;
    private Long profileId;
    private boolean visaLike = false;
    private String merchantId;

    // --- Getters & Setters ---
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getTerminalId() { return terminalId; }
    public void setTerminalId(String terminalId) { this.terminalId = terminalId; }

    public String getPan() { return pan; }
    public void setPan(String pan) { this.pan = pan; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getStan() { return stan; }
    public void setStan(String stan) { this.stan = stan; }

    public String getRrn() { return rrn; }
    public void setRrn(String rrn) { this.rrn = rrn; }

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }

    public boolean isVisaLike() { return visaLike; }
    public void setVisaLike(boolean visaLike) { this.visaLike = visaLike; }

    public String getMerchantId() { return merchantId; }
    public void setMerchantId(String merchantId) { this.merchantId = merchantId; }
}
