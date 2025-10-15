package com.powsybl.iidm.network.extensions;

import java.util.List;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface Smacc {

    String getName();

    MeasurementPoint getHighVoltageMeasurementPoint();

    MeasurementPoint getLowVoltageMeasurementPoint();

    List<ControlVoltageLevel> getHighVoltageControlVoltageLevels();

    Optional<ControlVoltageLevel> getHighVoltageControlVoltageLevel(String id);

    List<ControlVoltageLevel> getLowVoltageControlVoltageLevels();

    Optional<ControlVoltageLevel> getLowVoltageControlVoltageLevel(String id);
}
