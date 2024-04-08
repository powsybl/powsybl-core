package com.powsybl.iidm.network;

public interface AreaType extends Identifiable<AreaType> {
    @Override
    default IdentifiableType getType() {
        return IdentifiableType.AREA_TYPE;
    }
}
