package org.rtportfolio.model;

public final class SymbolPub {
    private  String symbol;
    private  double expectedReturn;
    private  double standardVar;
    private  double currentPx;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getExpectedReturn() {
        return expectedReturn;
    }

    public void setExpectedReturn(double expectedReturn) {
        this.expectedReturn = expectedReturn;
    }

    public double getStandardVar() {
        return standardVar;
    }

    public void setStandardVar(double standardVar) {
        this.standardVar = standardVar;
    }

    public double getCurrentPx() {
        return currentPx;
    }

    public void setCurrentPx(double currentPx) {
        this.currentPx = currentPx;
    }

}
