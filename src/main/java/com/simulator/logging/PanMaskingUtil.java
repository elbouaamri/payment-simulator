package com.simulator.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to mask PAN for safe logging.
 * Example: 4111111111111111 → 411111******1111
 */
public final class PanMaskingUtil {

    private PanMaskingUtil() {}

    private static final Logger log = LoggerFactory.getLogger(PanMaskingUtil.class);

    /**
     * Mask a PAN, keeping the first 6 and last 4 digits visible.
     */
    public static String mask(String pan) {
        if (pan == null || pan.length() < 10) return pan;
        int len = pan.length();
        String prefix = pan.substring(0, 6);
        String suffix = pan.substring(len - 4);
        String masked = prefix + "*".repeat(len - 10) + suffix;
        return masked;
    }
}
