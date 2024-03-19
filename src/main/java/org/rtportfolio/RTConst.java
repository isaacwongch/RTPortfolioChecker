package org.rtportfolio;

import java.util.Arrays;

public class RTConst {
    public static final String ID = "ID";
    public static final String TICKER = "TICKER";
    public static final String INSTRUMENT_TYPE = "INSTRUMENT_TYPE";
    public static final String STRIKE = "STRIKE";
    public static final String MATURITY_DATE = "MATURITY_DATE";
    public static final String UNDERLYING = "UNDERLYING";
    public static final String HST_CLOSE = "HST_CLOSE";

    public static final String DELIMITER = ",";

    public static final double MARKET_PRICE_SCALED_FACTOR = Math.pow(10, -4);

    public static final int MSG_POSITION_SYMBOL_SIZE = 24;
    public static final int MSG_POSITION_PRICE_SIZE = Long.BYTES;
    public static final int MSG_POSITION_QTY_SIZE = Integer.SIZE;
    public static final int MSG_POSITION_MARKET_VALUE_SIZE = Long.BYTES;
    public static final byte PAD = (byte) ' ';
    public static final byte[] THREE_PADS = new byte[3];
    public static final byte IS_UPDATED_BYTE = 'Y';
    public static final byte NOT_UPDATED_BYTE = 'N';
    public static final int MSG_POSITION_BEFORE_UPDATE_FLAG_SIZE = MSG_POSITION_SYMBOL_SIZE + MSG_POSITION_PRICE_SIZE + MSG_POSITION_QTY_SIZE + MSG_POSITION_MARKET_VALUE_SIZE;
    public static final int MSG_POSITION_TOTAL_SIZE = MSG_POSITION_BEFORE_UPDATE_FLAG_SIZE + Integer.BYTES;

    public static final int INIT_EXPECT_MESSAGE_LEN = Integer.SIZE;

    static {
        Arrays.fill(THREE_PADS, PAD);
    }

}
