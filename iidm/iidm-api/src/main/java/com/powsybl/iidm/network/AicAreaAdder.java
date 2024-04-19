package com.powsybl.iidm.network;

public interface AicAreaAdder extends AreaAdder {

    AicAreaAdder setAcNetInterchangeTarget(double acNetInterchangeTarget);

    AicAreaAdder setAcNetInterchangeTolerance(double acNetInterchangeTolerance);

    @Override
    AicArea add();
}
