package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AicArea;
import com.powsybl.iidm.network.AicAreaAdder;
import com.powsybl.iidm.network.impl.util.Ref;

public class AicAreaAdderImpl extends AreaAdderImpl implements AicAreaAdder {

    private double acNetInterchangeTarget;

    private double acNetInterchangeTolerance;

    AicAreaAdderImpl(Ref<NetworkImpl> networkRef) {
        super(networkRef);
    }

    @Override
    public AicAreaAdder setAcNetInterchangeTarget(double acNetInterchangeTarget) {
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        return this;
    }

    @Override
    public AicAreaAdder setAcNetInterchangeTolerance(double acNetInterchangeTolerance) {
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
        return this;
    }

    @Override
    public AicArea add() {
        String id = checkAndGetUniqueId();
        AicAreaImpl aicArea = new AicAreaImpl(networkRef, id, getName(), isFictitious(), areaType, acNetInterchangeTarget, acNetInterchangeTolerance);
        getNetwork().getIndex().checkAndAdd(aicArea);
        getNetwork().getListeners().notifyCreation(aicArea);
        return aicArea;
    }

    @Override
    protected String getTypeDescription() {
        return "AicArea";
    }
}
