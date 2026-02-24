package com.simulator.iso;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Parses raw ISO 8583 bytes back into an ISOMsg.
 */
@Component
public class IsoMessageParser {

    private static final Logger log = LoggerFactory.getLogger(IsoMessageParser.class);
    private final IsoMessageBuilder builder;

    public IsoMessageParser(IsoMessageBuilder builder) {
        this.builder = builder;
    }

    /**
     * Parse a hex-encoded ISO message string.
     */
    public ISOMsg parseHex(String hex) throws ISOException {
        byte[] data = hexToBytes(hex);
        ISOMsg msg = new ISOMsg();
        msg.setPackager(builder.getPackager());
        msg.unpack(data);
        return msg;
    }

    /**
     * Extract all set fields into a human-readable map.
     */
    public Map<String, String> toFieldMap(ISOMsg msg) throws ISOException {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("MTI", msg.getMTI());
        for (int i = 1; i <= msg.getMaxField(); i++) {
            if (msg.hasField(i)) {
                fields.put("F" + String.format("%02d", i), msg.getString(i));
            }
        }
        return fields;
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
