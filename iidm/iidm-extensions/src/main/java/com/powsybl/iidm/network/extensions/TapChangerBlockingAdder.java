package com.powsybl.iidm.network.extensions;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface TapChangerBlockingAdder {

    TapChangerBlockingAdder withName(String name);

    MeasurementPointAdder<TapChangerBlockingAdder> newMeasurementPoint();

    ControlVoltageLevelAdder<TapChangerBlockingAdder> newControlVoltageLevel();

    TapChangerBlockingsAdder add();
}
