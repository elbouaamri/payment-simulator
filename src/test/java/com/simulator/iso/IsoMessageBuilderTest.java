package com.simulator.iso;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IsoMessageBuilderTest {

    private IsoMessageBuilder builder;

    @BeforeEach
    void setUp() throws Exception {
        builder = new IsoMessageBuilder();
        builder.init();
    }

    @Test
    @DisplayName("Build authorization request – verify critical fields")
    void testBuildAuthorizationRequest() throws Exception {
        ISOMsg msg = builder.buildAuthorizationRequest(
                "4111111111111111", "000000", 10000,
                "2812", "TERM0001", "MERCH001",
                "123456", "000000000001", "504", false);

        assertEquals("0200", msg.getMTI());
        assertEquals("4111111111111111", msg.getString(2));
        assertEquals("000000", msg.getString(3));
        assertEquals("000000010000", msg.getString(4));
        assertEquals("2812", msg.getString(14));
        assertEquals("123456", msg.getString(11));
        assertEquals("000000000001", msg.getString(37));
        assertEquals("TERM0001", msg.getString(41));
        assertEquals("504", msg.getString(49));
    }

    @Test
    @DisplayName("Build Visa-like request uses MTI 0100")
    void testVisaLikeMti() throws Exception {
        ISOMsg msg = builder.buildAuthorizationRequest(
                "4111111111111111", "000000", 10000,
                "2812", "TERM0001", null,
                null, null, "504", true);

        assertEquals("0100", msg.getMTI());
    }

    @Test
    @DisplayName("Build reversal request – MTI 0400")
    void testBuildReversalRequest() throws Exception {
        ISOMsg msg = builder.buildReversalRequest(
                "4111111111111111", 10000, "2812",
                "TERM0001", null, null, null, "504", false);

        assertEquals("0400", msg.getMTI());
        assertEquals("4111111111111111", msg.getString(2));
    }

    @Test
    @DisplayName("Build response from request – MTI mapping & response code")
    void testBuildResponse() throws Exception {
        ISOMsg request = builder.buildAuthorizationRequest(
                "4111111111111111", "000000", 10000,
                "2812", "TERM0001", null,
                null, null, "504", false);

        ISOMsg response = builder.buildResponse(request, "00", false);

        assertEquals("0210", response.getMTI());
        assertEquals("00", response.getString(39));
        // Request fields should be carried over
        assertEquals("4111111111111111", response.getString(2));
    }

    @Test
    @DisplayName("Pack and extract fields round-trip")
    void testPackAndExtract() throws Exception {
        ISOMsg msg = builder.buildAuthorizationRequest(
                "5500000000000004", "000000", 25000,
                "2712", "TERM0002", null,
                "654321", null, "504", false);

        String hex = builder.packToHex(msg);
        assertNotNull(hex);
        assertFalse(hex.isEmpty());

        Map<String, String> fields = builder.extractFields(msg);
        assertEquals("0200", fields.get("MTI"));
        assertEquals("5500000000000004", fields.get("F2"));
        assertEquals("000000025000", fields.get("F4"));
    }
}
