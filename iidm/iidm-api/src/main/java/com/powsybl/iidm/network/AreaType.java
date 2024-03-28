package com.powsybl.iidm.network;

public interface AreaType<I extends AreaType<I>> extends Identifiable<I> {
    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA_TYPE;
    }
}
