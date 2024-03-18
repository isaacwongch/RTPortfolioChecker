package org.rtportfolio.util;


import org.rtportfolio.model.PriceUpdate;

public class PriceUpdateObjectCreator implements RTObjectCreator<PriceUpdate> {
    @Override
    public PriceUpdate create() {
        return new PriceUpdate();
    }
}
