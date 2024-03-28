package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;

public class AreaTypeImpl extends AbstractIdentifiable implements AreaType {
    private final Ref<NetworkImpl> networkRef;

    public AreaTypeImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "AreaType";
    }
}
