package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaTypeAdder;
import com.powsybl.iidm.network.impl.util.Ref;

public class AreaTypeAdderImpl extends AbstractIdentifiableAdder<AreaTypeAdderImpl> implements AreaTypeAdder {

    private final Ref<NetworkImpl> networkRef;

    AreaTypeAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
    }

    @Override
    public AreaTypeImpl add() {
        String id = checkAndGetUniqueId();
        AreaTypeImpl areaType = new AreaTypeImpl(networkRef, id, getName(), isFictitious());
        getNetwork().getIndex().checkAndAdd(areaType);
        getNetwork().getListeners().notifyCreation(areaType);
        return areaType;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "AreaType";
    }
}
