package org.rtportfolio.model;

import java.util.List;

public final class Portfolio {
    private final List<Position> positions;
    private double totalNav;

    public Portfolio(final List<Position> positions) {
        this.positions = positions;
    }

    public void refreshTotalNav() {
        this.totalNav = positions.stream().mapToDouble(p -> p.getPositionMarketValue()).sum();
    }
}
