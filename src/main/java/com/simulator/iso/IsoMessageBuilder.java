package com.simulator.iso;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static com.simulator.iso.IsoFieldConstants.*;

/**
 * Builds ISO 8583 messages from transaction parameters.
 */
@Component
public class IsoMessageBuilder {

    private static final Logger log = LoggerFactory.getLogger(IsoMessageBuilder.class);
    private GenericPackager packager;
    private final Random random = new Random();

    @PostConstruct
    public void init() throws ISOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("iso8583.xml");
        if (is == null)
            throw new ISOException("iso8583.xml packager file not found on classpath");
        packager = new GenericPackager(is);
        log.info("ISO 8583 GenericPackager loaded successfully");
    }

    public GenericPackager getPackager() {
        return packager;
    }

    /**
     * Build an authorization request (0200).
     */
    public ISOMsg buildAuthorizationRequest(String pan, String processingCode, long amount,
            String expiry, String terminalId, String merchantId,
            String stan, String rrn, String currency,
            long amountSettlement, long amountCardholderBilling,
            String merchantType,
            boolean visaLike) throws ISOException {
        String mti = visaLike ? "0100" : "0200";
        return buildRequest(mti, pan, processingCode, amount, expiry, terminalId, merchantId, stan, rrn, currency,
                amountSettlement, amountCardholderBilling, merchantType);
    }

    /**
     * Build a reversal request (0400).
     */
    public ISOMsg buildReversalRequest(String pan, long amount, String expiry,
            String terminalId, String merchantId,
            String stan, String rrn, String currency,
            long amountSettlement, long amountCardholderBilling,
            String merchantType,
            boolean visaLike) throws ISOException {
        String mti = "0400";
        return buildRequest(mti, pan, PC_PURCHASE, amount, expiry, terminalId, merchantId, stan, rrn, currency,
                amountSettlement, amountCardholderBilling, merchantType);
    }

    /**
     * Build response from request by copying fields and adding response code.
     */
    public ISOMsg buildResponse(ISOMsg request, String responseCode, boolean visaLike) throws ISOException {
        ISOMsg response = (ISOMsg) request.clone();
        response.setPackager(packager);

        String requestMti = request.getMTI();
        String responseMti = computeResponseMti(requestMti);
        response.setMTI(responseMti);
        response.set(RESPONSE_CODE, responseCode);
        response.set(AUTH_CODE, generateAuthCode());

        return response;
    }

    /**
     * Pack message to byte array (hex-encoded string).
     */
    public String packToHex(ISOMsg msg) throws ISOException {
        byte[] packed = msg.pack();
        return bytesToHex(packed);
    }

    /**
     * Extract all set fields from an ISOMsg into a Map.
     */
    public Map<String, String> extractFields(ISOMsg msg) throws ISOException {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("MTI", msg.getMTI());
        for (int i = 1; i <= msg.getMaxField(); i++) {
            if (msg.hasField(i)) {
                fields.put("F" + i, msg.getString(i));
            }
        }
        return fields;
    }

    // --- Private helpers ---

    private ISOMsg buildRequest(String mti, String pan, String processingCode, long amount,
            String expiry, String terminalId, String merchantId,
            String stan, String rrn, String currency,
            long amountSettlement, long amountCardholderBilling,
            String merchantType) throws ISOException {
        ISOMsg msg = new ISOMsg();
        msg.setPackager(packager);
        msg.setMTI(mti);

        msg.set(PAN, pan);
        msg.set(PROCESSING_CODE, processingCode);
        msg.set(AMOUNT, String.format("%012d", amount));
        msg.set(AMOUNT_SETTLEMENT, String.format("%012d", amountSettlement));
        msg.set(AMOUNT_CARDHOLDER_BILL, String.format("%012d", amountCardholderBilling));

        LocalDateTime now = LocalDateTime.now();
        msg.set(TRANSMISSION_DT, now.format(DateTimeFormatter.ofPattern("MMddHHmmss")));
        msg.set(LOCAL_TIME, now.format(DateTimeFormatter.ofPattern("HHmmss")));
        msg.set(LOCAL_DATE, now.format(DateTimeFormatter.ofPattern("MMdd")));

        if (expiry != null && !expiry.isEmpty()) {
            msg.set(EXPIRATION, expiry);
        }
        if (stan != null && !stan.isEmpty()) {
            msg.set(STAN, String.format("%06d", Integer.parseInt(stan)));
        } else {
            msg.set(STAN, String.format("%06d", random.nextInt(999999)));
        }
        if (rrn != null && !rrn.isEmpty()) {
            msg.set(RRN, rrn);
        } else {
            msg.set(RRN, String.format("%012d", random.nextLong(999999999999L)));
        }
        if (terminalId != null) {
            msg.set(TERMINAL_ID, String.format("%-8s", terminalId).substring(0, 8));
        }
        if (merchantId != null) {
            msg.set(MERCHANT_ID, String.format("%-15s", merchantId).substring(0, 15));
        }
        if (currency != null) {
            msg.set(CURRENCY_CODE, currency);
        }
        msg.set(POS_ENTRY_MODE, "051"); // chip
        msg.set(POS_CONDITION, "00");
        if (merchantType != null) {
            msg.set(MERCHANT_TYPE, merchantType);
        }

        return msg;
    }

    private String computeResponseMti(String requestMti) {
        return switch (requestMti) {
            case "0100" -> "0110";
            case "0200" -> "0210";
            case "0400" -> "0410";
            case "0420" -> "0430";
            default -> {
                int mtiInt = Integer.parseInt(requestMti);
                yield String.format("%04d", mtiInt + 10);
            }
        };
    }

    private String generateAuthCode() {
        return String.format("%06d", random.nextInt(999999));
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
