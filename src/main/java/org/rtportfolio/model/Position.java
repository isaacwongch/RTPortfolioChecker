package org.rtportfolio.model;

public final class Position {
    private final Instrument instrument;
    private final int positionSize;
    private double symbolCurrentValPerShare;
    private double positionMarketValue;

    public Position(final Instrument instrument, final int positionSize) {
        this.instrument = instrument;
        this.positionSize = positionSize;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public int getPositionSize() {
        return positionSize;
    }

    public double getSymbolCurrentValPerShare() {
        return symbolCurrentValPerShare;
    }

    public void setSymbolCurrentValPerShareAndDependent(double symbolCurrentValPerShare) {
        this.symbolCurrentValPerShare = symbolCurrentValPerShare;
        this.positionMarketValue = this.symbolCurrentValPerShare * this.positionSize;
    }

    public double getPositionMarketValue() {
        return positionMarketValue;
    }

}
