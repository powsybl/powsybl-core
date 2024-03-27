package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Area;
import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Objects;

public class AreaImpl extends AbstractIdentifiable implements Area {
    private final Ref<NetworkImpl> networkRef;
    private final AreaType areaType;
    private final double acNetInterchangeTarget;
    private final double acNetInterchangeTolerance;

    public AreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType) {
        this(ref, id, name, fictitious, areaType, Double.NaN, Double.NaN);
    }

    public AreaImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious, AreaType areaType, double acNetInterchangeTarget,
                    double acNetInterchangeTolerance) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.areaType = Objects.requireNonNull(areaType);
        this.acNetInterchangeTarget = acNetInterchangeTarget;
        this.acNetInterchangeTolerance = acNetInterchangeTolerance;
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Area";
    }

    @Override
    public AreaType getAreaType() {
        return areaType;
    }

    @Override
    public double getAcNetInterchangeTarget() {
        return acNetInterchangeTarget;
    }

    @Override
    public double getAcNetInterchangeTolerance() {
        return acNetInterchangeTolerance;
    }
}
