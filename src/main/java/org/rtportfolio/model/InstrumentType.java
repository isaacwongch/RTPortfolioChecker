package org.rtportfolio.model;

public enum InstrumentType {
    STOCK(1),
    CALL_OPTION(2),
    PUT_OPTION(3),

    UNKNOWN(0);

    private int id;

    InstrumentType(int id) {
        this.id = id;
    }

    public static InstrumentType getById(int id) {
        for (InstrumentType e : values()) {
            if (e.id == id) return e;
        }
        return UNKNOWN;
    }
}
