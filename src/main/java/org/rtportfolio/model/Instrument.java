package org.rtportfolio.model;

public class Instrument {
    private final String symbol;
    private final InstrumentType instrumentType;
    private final long strike;
    private final String maturityDate;

    public Instrument(String symbol, InstrumentType instrumentType, long strike, String maturityDate) {
        this.symbol = symbol;
        this.instrumentType = instrumentType;
        this.strike = strike;
        this.maturityDate = maturityDate;
    }

    public String getSymbol() {
        return symbol;
    }

    public InstrumentType getInstrumentType() {
        return instrumentType;
    }

    public long getStrike() {
        return strike;
    }

    public String getMaturityDate() {
        return maturityDate;
    }

}
