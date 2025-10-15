package com.powsybl.iidm.network.extensions;

import java.util.List;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface Acmc {

    String getName();

    MeasurementPoint getMeasurementPoint();

    List<ControlVoltageLevel> getControlVoltageLevels();

    Optional<ControlVoltageLevel> getControlVoltageLevel(String id);
}
