package com.simulator.iso;

/**
 * Constants for ISO 8583 field IDs – avoids magic numbers throughout the codebase.
 */
public final class IsoFieldConstants {

    private IsoFieldConstants() {}

    public static final int MTI            = 0;
    public static final int PAN            = 2;
    public static final int PROCESSING_CODE = 3;
    public static final int AMOUNT         = 4;
    public static final int TRANSMISSION_DT = 7;
    public static final int STAN           = 11;
    public static final int LOCAL_TIME     = 12;
    public static final int LOCAL_DATE     = 13;
    public static final int EXPIRATION     = 14;
    public static final int POS_ENTRY_MODE = 22;
    public static final int CARD_SEQ_NUM   = 23;
    public static final int POS_CONDITION  = 25;
    public static final int ACQUIRING_INST = 32;
    public static final int TRACK2         = 35;
    public static final int RRN            = 37;
    public static final int AUTH_CODE      = 38;
    public static final int RESPONSE_CODE  = 39;
    public static final int TERMINAL_ID    = 41;
    public static final int MERCHANT_ID    = 42;
    public static final int ACCEPTOR_NAME  = 43;
    public static final int CURRENCY_CODE  = 49;
    public static final int PIN_BLOCK      = 52;
    public static final int ADDITIONAL_AMT = 54;
    public static final int EMV_DATA       = 55;

    // --- Processing codes ---
    public static final String PC_PURCHASE   = "000000";
    public static final String PC_REFUND     = "200000";
    public static final String PC_VOID       = "020000";
    public static final String PC_BALANCE    = "310000";
}
