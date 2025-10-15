package com.powsybl.iidm.network.extensions;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface SmaccAdder {

    SmaccAdder withName(String name);

    MeasurementPointAdder<SmaccAdder> newHighVoltageMeasurementPoint();

    MeasurementPointAdder<SmaccAdder> newLowVoltageMeasurementPoint();

    ControlVoltageLevelAdder<SmaccAdder> newHighVoltageControlVoltageLevel();

    ControlVoltageLevelAdder<SmaccAdder> newLowVoltageControlVoltageLevel();

    SmaccsAdder add();

}

