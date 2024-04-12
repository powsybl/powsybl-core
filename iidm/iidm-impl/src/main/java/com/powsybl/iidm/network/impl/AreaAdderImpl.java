package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaAdder;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.HashMap;
import java.util.stream.Collectors;

class AreaAdderImpl extends AbstractIdentifiableAdder<AreaAdderImpl> implements AreaAdder {

    private final Ref<NetworkImpl> networkRef;

    private final AreaType areaType;

    AreaAdderImpl(Ref<NetworkImpl> networkRef, AreaType areaType) {
        this.networkRef = networkRef;
        this.areaType = areaType;
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        AreaImpl area = new AreaImpl(networkRef, id, getName(), isFictitious(), areaType);
        getNetwork().getAllAreasMap().computeIfAbsent(areaType, k -> new HashMap<>()).put(id, area);
        return area;
    }

    @Override
    protected String checkAndGetUniqueId() {
        return checkAndGetUniqueId(getNetwork().getAllAreasMap().values().stream().flatMap(v -> v.keySet().stream()).collect(Collectors.toSet())::contains);
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }
}

