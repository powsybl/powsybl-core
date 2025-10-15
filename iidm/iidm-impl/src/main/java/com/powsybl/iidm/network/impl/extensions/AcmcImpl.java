package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlVoltageLevel;
import com.powsybl.iidm.network.extensions.Acmc;
import com.powsybl.iidm.network.extensions.MeasurementPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class AcmcImpl implements Acmc {

    private AcmcsImpl acmcs;

    private final String name;

    private final MeasurementPoint measurementPoint;

    private final List<ControlVoltageLevel> controlVoltageLevels;

    AcmcImpl(String name, MeasurementPoint measurementPoint, List<ControlVoltageLevel> controlVoltageLevels) {
        this.name = Objects.requireNonNull(name);
        this.measurementPoint = Objects.requireNonNull(measurementPoint);
        this.controlVoltageLevels = Objects.requireNonNull(controlVoltageLevels);
    }

    void setAcmcs(AcmcsImpl acmcs) {
        this.acmcs = Objects.requireNonNull(acmcs);
    }

    public AcmcsImpl getAcmcs() {
        return acmcs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MeasurementPoint getMeasurementPoint() {
        return measurementPoint;
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
