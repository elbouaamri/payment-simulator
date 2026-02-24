package com.simulator.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

/**
 * Visa-like simulation layer.
 *
 * <p>Provides MTI remapping for Visa-standard message types and generates
 * a fake "cryptogram" for testing purposes.</p>
 *
 * <h3>What is simulated vs real:</h3>
 * <ul>
 *   <li><b>Simulated</b>: cryptogram is a simple SHA-256 hash of PAN+amount+timestamp – NOT real ARQC/TC.</li>
 *   <li><b>Simulated</b>: EMV field 55 is populated with a fake TLV string.</li>
 *   <li><b>Real mapping</b>: MTI 0100→0110, 0200→0210, 0400→0410 follow Visa specs.</li>
 * </ul>
 */
@Service
public class VisaLikeService {

    private static final Logger log = LoggerFactory.getLogger(VisaLikeService.class);

    /** Standard Visa MTI request → response mapping */
    private static final Map<String, String> MTI_MAP = Map.of(
            "0100", "0110",
            "0200", "0210",
            "0400", "0410",
            "0420", "0430"
    );

    /**
     * Returns the Visa response MTI for a given request MTI.
     */
    public String getResponseMti(String requestMti) {
        return MTI_MAP.getOrDefault(requestMti, String.valueOf(Integer.parseInt(requestMti) + 10));
    }

    /**
     * Determine if a PAN represents a Visa-like card (starts with '4').
     */
    public boolean isVisaCard(String pan) {
        return pan != null && pan.startsWith("4");
    }

    /**
     * Generate a fake cryptogram (simulated ARQC) for testing.
     * This is NOT a real EMV cryptogram – it's a simple hash for demonstration.
     *
     * @param pan    Card PAN
     * @param amount Transaction amount
     * @return hex-encoded string (16 chars, truncated SHA-256)
     */
    public String generateFakeCryptogram(String pan, long amount) {
        try {
            String input = pan + "|" + amount + "|" + System.currentTimeMillis();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString(); // 16 hex chars
        } catch (Exception e) {
            log.warn("Failed to generate fake cryptogram", e);
            return "0000000000000000";
        }
    }

    /**
     * Generate fake EMV TLV data for field 55 (simulated).
     * Tags: 9F26 (ARQC), 9F27 (CID), 9F10 (IAD).
     */
    public String generateFakeEmvData(String pan, long amount) {
        String cryptogram = generateFakeCryptogram(pan, amount);
        // Simplified TLV: Tag-Length-Value (not real BER-TLV encoding)
        return "9F2608" + cryptogram + "9F27010080" + "9F100706011203A00000";
    }
}
