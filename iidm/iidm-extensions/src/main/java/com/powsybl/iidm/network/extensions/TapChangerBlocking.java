package com.powsybl.iidm.network.extensions;

import java.util.List;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public interface TapChangerBlocking {

    String getName();

    List<MeasurementPoint> getMeasurementPoints();

    Optional<MeasurementPoint> getMeasurementPoint(String id);

    List<ControlVoltageLevel> getControlVoltageLevels();

    Optional<ControlVoltageLevel> getControlVoltageLevel(String id);
}
