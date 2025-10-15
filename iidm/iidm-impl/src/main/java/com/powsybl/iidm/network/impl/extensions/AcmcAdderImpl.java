package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class AcmcAdderImpl implements AcmcAdder {

    private final AcmcsAdderImpl parent;

    private String name;

    private MeasurementPointImpl measurementPoint;

    private final List<ControlVoltageLevel> controlVoltageLevels = new ArrayList<>();

    AcmcAdderImpl(AcmcsAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    public AcmcsAdderImpl getParent() {
        return parent;
    }

    @Override
    public AcmcAdderImpl withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    void setMeasurementPoint(MeasurementPointImpl measurementPoint) {
        this.measurementPoint = Objects.requireNonNull(measurementPoint);
    }

    @Override
    public MeasurementPointAdder<AcmcAdder> newMeasurementPoint() {
        return new MeasurementPointAdderAcmcImpl(this);
    }

    void addControlVoltageLevel(ControlVoltageLevelImpl controlVoltageLevel) {
        controlVoltageLevels.add(Objects.requireNonNull(controlVoltageLevel));
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<AcmcAdder> newControlVoltageLevel() {
        return new ControlVoltageLevelAdderAcmcImpl(this);
    }

    @Override
    public AcmcsAdderImpl add() {
        if (name == null) {
            throw new PowsyblException("Acmc name is not set");
        }
        if (measurementPoint == null) {
            throw new PowsyblException("Measurement point is not set for acmc '" + name + "'");
        }
        if (controlVoltageLevels.isEmpty()) {
            throw new PowsyblException("Empty control voltage level list for acmc '" + name + "'");
        }
        AcmcImpl acmc = new AcmcImpl(name, measurementPoint, controlVoltageLevels);
        parent.addAcmc(acmc);
        return parent;
    }
}
