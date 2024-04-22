package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaAdder;
import com.powsybl.iidm.network.impl.util.Ref;

public class AreaAdderImpl extends AbstractAreaAdder<AreaAdderImpl> implements AreaAdder {

    AreaAdderImpl(Ref<NetworkImpl> networkRef) {
        super(networkRef);
    }

    @Override
    public Area add() {
        String id = checkAndGetUniqueId();
        AreaImpl area = new AreaImpl(getNetworkRef(), id, getName(), isFictitious(), getAreaType());
        getNetwork().getIndex().checkAndAdd(area);
        getNetwork().getListeners().notifyCreation(area);
        return area;
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }
}

