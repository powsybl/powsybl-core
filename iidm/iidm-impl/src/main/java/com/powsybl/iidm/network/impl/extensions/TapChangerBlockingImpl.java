package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.TapChangerBlocking;
import com.powsybl.iidm.network.extensions.ControlVoltageLevel;
import com.powsybl.iidm.network.extensions.MeasurementPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class TapChangerBlockingImpl implements TapChangerBlocking {

    private TapChangerBlockingsImpl tcbs;

    private final String name;

    private final List<MeasurementPoint> measurementPoints;

    private final List<ControlVoltageLevel> controlVoltageLevels;

    TapChangerBlockingImpl(String name, List<MeasurementPoint> measurementPoints, List<ControlVoltageLevel> controlVoltageLevels) {
        this.name = Objects.requireNonNull(name);
        this.measurementPoints = Objects.requireNonNull(measurementPoints);
        this.controlVoltageLevels = Objects.requireNonNull(controlVoltageLevels);
    }

    void setTapChangerBlockings(TapChangerBlockingsImpl tcbs) {
        this.tcbs = Objects.requireNonNull(tcbs);
    }

    public TapChangerBlockingsImpl getTapChangerBlockings() {
        return tcbs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<MeasurementPoint> getMeasurementPoints() {
        return Collections.unmodifiableList(measurementPoints);
    }

    @Override
    public Optional<MeasurementPoint> getMeasurementPoint(String id) {
        Objects.requireNonNull(id);
        return measurementPoints.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @Override
    public List<ControlVoltageLevel> getControlVoltageLevels() {
        return Collections.unmodifiableList(controlVoltageLevels);
    }

    @Override
    public Optional<ControlVoltageLevel> getControlVoltageLevel(String id) {
        Objects.requireNonNull(id);
        return controlVoltageLevels.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

}
