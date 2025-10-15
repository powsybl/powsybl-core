package com.powsybl.iidm.network.extensions;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface AcmcAdder {

    AcmcAdder withName(String name);

    MeasurementPointAdder<AcmcAdder> newMeasurementPoint();

    ControlVoltageLevelAdder<AcmcAdder> newControlVoltageLevel();

    AcmcsAdder add();
}
