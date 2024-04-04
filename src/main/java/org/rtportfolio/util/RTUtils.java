package org.rtportfolio.util;

import org.rtportfolio.RTConst;

public class RTUtils {
    public static int getMessageSize(final int numberOfPos){
        return Integer.BYTES + Integer.BYTES + RTConst.MSG_SYMBOL_SIZE + Long.BYTES + numberOfPos * RTConst.MSG_POSITION_BEFORE_UPDATE_FLAG_SIZE + Long.BYTES;
    }
}
