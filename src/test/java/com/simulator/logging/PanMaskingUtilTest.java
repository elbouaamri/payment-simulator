package com.simulator.logging;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PanMaskingUtilTest {

    @Test
    void testMask16Digits() {
        assertEquals("411111******1111", PanMaskingUtil.mask("4111111111111111"));
    }

    @Test
    void testMask19Digits() {
        assertEquals("411111*********1111", PanMaskingUtil.mask("4111111111111111111"));
    }

    @Test
    void testMaskShortPan() {
        assertEquals("1234567890", PanMaskingUtil.mask("1234567890")); // 10 chars → 6+4, 0 stars
    }

    @Test
    void testMaskNull() {
        assertNull(PanMaskingUtil.mask(null));
    }

    @Test
    void testMaskTooShort() {
        assertEquals("12345", PanMaskingUtil.mask("12345")); // less than 10, returned as-is
    }
}
