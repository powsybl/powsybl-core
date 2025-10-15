package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.extensions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
public class SmaccAdderImpl implements SmaccAdder {

    private final SmaccsAdderImpl parent;

    private String name;

    private MeasurementPointImpl highVoltagemeasurementPoint;

    private MeasurementPointImpl lowVoltagemeasurementPoint;

    private final List<ControlVoltageLevel> highVoltageControlVoltageLevels = new ArrayList<>();

    private final List<ControlVoltageLevel> lowVoltageControlVoltageLevels = new ArrayList<>();

    SmaccAdderImpl(SmaccsAdderImpl parent) {
        this.parent = Objects.requireNonNull(parent);
    }

    public SmaccsAdderImpl getParent() {
        return parent;
    }

    @Override
    public SmaccAdderImpl withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    void setHighVoltageMeasurementPoint(MeasurementPointImpl highVoltagemeasurementPoint) {
        this.highVoltagemeasurementPoint = Objects.requireNonNull(highVoltagemeasurementPoint);
    }

    void setLowVoltageMeasurementPoint(MeasurementPointImpl lowVoltagemeasurementPoint) {
        this.lowVoltagemeasurementPoint = Objects.requireNonNull(lowVoltagemeasurementPoint);
    }

    @Override
    public AbstractMeasurementPointAdderImpl<SmaccAdder> newHighVoltageMeasurementPoint() {
        return new HighVoltageMeasurementPointAdderSmaccImpl(this);
    }

    @Override
    public AbstractMeasurementPointAdderImpl<SmaccAdder> newLowVoltageMeasurementPoint() {
        return new LowVoltageMeasurementPointAdderSmaccImpl(this);
    }

    void addHighVoltageControlVoltageLevel(ControlVoltageLevelImpl highVoltageControlVoltageLevel) {
        highVoltageControlVoltageLevels.add(Objects.requireNonNull(highVoltageControlVoltageLevel));
    }

    void addLowVoltageControlVoltageLevel(ControlVoltageLevelImpl lowVoltageControlVoltageLevel) {
        lowVoltageControlVoltageLevels.add(Objects.requireNonNull(lowVoltageControlVoltageLevel));
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<SmaccAdder> newHighVoltageControlVoltageLevel() {
        return new HighVoltageControlVoltageLevelAdderSmaccImpl(this);
    }

    @Override
    public AbstractControlVoltageLevelAdderImpl<SmaccAdder> newLowVoltageControlVoltageLevel() {
        return new LowVoltageControlVoltageLevelAdderSmaccImpl(this);
    }

    @Override
    public SmaccsAdderImpl add() {
        if (name == null) {
            throw new PowsyblException("Smacc name is not set");
        }
        if (highVoltagemeasurementPoint == null) {
            throw new PowsyblException("High voltage measurement point is not set for smacc '" + name + "'");
        }
        if (lowVoltagemeasurementPoint == null) {
            throw new PowsyblException("Low voltage measurement point is not set for smacc '" + name + "'");
        }
        if (highVoltageControlVoltageLevels.isEmpty()) {
            throw new PowsyblException("Empty high voltage control voltage level list for smacc '" + name + "'");
        }
        if (lowVoltageControlVoltageLevels.isEmpty()) {
            throw new PowsyblException("Empty low voltage control voltage level list for smacc '" + name + "'");
        }
        SmaccImpl smacc = new SmaccImpl(name, highVoltagemeasurementPoint, lowVoltagemeasurementPoint,
                highVoltageControlVoltageLevels, lowVoltageControlVoltageLevels);
        parent.addSmacc(smacc);
        return parent;
    }
}
