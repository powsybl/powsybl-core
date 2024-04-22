package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

abstract class AbstractAreaAdder<T extends AbstractAreaAdder<T>> extends AbstractIdentifiableAdder<T> {

    private final Ref<NetworkImpl> networkRef;

    private AreaType areaType;

    AbstractAreaAdder(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
    }

    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    public T setAreaType(AreaType areaType) {
        this.areaType = areaType;
        return (T) this;
    }

    protected Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    protected AreaType getAreaType() {
        return areaType;
    }
}
