package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.MeasurementPoint;
import com.powsybl.iidm.network.extensions.TapChangerBlockingAdder;
import com.powsybl.iidm.network.extensions.ControlVoltageLevel;
import com.powsybl.iidm.network.extensions.MeasurementPointAdder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class TapChangerBlockingAdderImpl implements TapChangerBlockingAdder {

    private final TapChangerBlockingsAdderImpl parent;

    private String name;

    private List<MeasurementPoint> measurementPoints = new ArrayList<>();

    private final List<ControlVoltageLevel> controlVoltageLevels = new ArrayList<>();

    TapChangerBlockingAdderImpl(TapChangerBlockingsAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    public TapChangerBlockingsAdderImpl getParent() {
        return parent;
    }

    @Override
    public TapChangerBlockingAdderImpl withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    void setMeasurementPoint(MeasurementPointImpl measurementPoint) {
        measurementPoints.add(Objects.requireNonNull(measurementPoint));
    }

    @Override
    public MeasurementPointAdder<TapChangerBlockingAdder> newMeasurementPoint() {
        return new MeasurementPointAdderTapChangerBlockingImpl(this);
    }

    void addControlVoltageLevel(ControlVoltageLevelImpl controlVoltageLevel) {
        controlVoltageLevels.add(Objects.requireNonNull(controlVoltageLevel));
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<TapChangerBlockingAdder> newControlVoltageLevel() {
        return new ControlVoltageLevelAdderTapChangerBlockingImpl(this);
    }

    @Override
    public TapChangerBlockingsAdderImpl add() {
        if (name == null) {
            throw new PowsyblException("TapChangerBlocking name is not set");
        }
        if (measurementPoints == null) {
            throw new PowsyblException("Measurement point is not set for tcb '" + name + "'");
        }
        if (controlVoltageLevels.isEmpty()) {
            throw new PowsyblException("Empty control voltage level list for tcb '" + name + "'");
        }
        TapChangerBlockingImpl tcb = new TapChangerBlockingImpl(name, measurementPoints, controlVoltageLevels);
        parent.addTapChangerBlocking(tcb);
        return parent;
    }
}
