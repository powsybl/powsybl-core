package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.extensions.ControlVoltageLevel;
import com.powsybl.iidm.network.extensions.Smacc;
import com.powsybl.iidm.network.extensions.MeasurementPoint;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Gautier Bureau {@literal <gautier.bureau at rte-france.com>}
 */
class SmaccImpl implements Smacc {

    private SmaccsImpl smaccs;

    private final String name;

    private final MeasurementPoint highVoltageMeasurementPoint;

    private final MeasurementPoint lowVoltageMeasurementPoint;

    private final List<ControlVoltageLevel> highVoltageControlVoltageLevels;

    private final List<ControlVoltageLevel> lowVoltageControlVoltageLevels;

    SmaccImpl(String name, MeasurementPoint highVoltageMeasurementPoint,
              MeasurementPoint lowVoltageMeasurementPoint,
              List<ControlVoltageLevel> highVoltageControlVoltageLevels,
              List<ControlVoltageLevel> lowVoltageControlVoltageLevels) {
        this.name = Objects.requireNonNull(name);
        this.highVoltageMeasurementPoint = Objects.requireNonNull(highVoltageMeasurementPoint);
        this.lowVoltageMeasurementPoint = Objects.requireNonNull(lowVoltageMeasurementPoint);
        this.highVoltageControlVoltageLevels = Objects.requireNonNull(highVoltageControlVoltageLevels);
        this.lowVoltageControlVoltageLevels = Objects.requireNonNull(lowVoltageControlVoltageLevels);
    }

    void setSmaccs(SmaccsImpl smaccs) {
        this.smaccs = Objects.requireNonNull(smaccs);
    }

    public SmaccsImpl getSmaccs() {
        return smaccs;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public MeasurementPoint getHighVoltageMeasurementPoint() {
        return highVoltageMeasurementPoint;
    }

    @Override
    public MeasurementPoint getLowVoltageMeasurementPoint() {
        return lowVoltageMeasurementPoint;
    }

    @Override
    public List<ControlVoltageLevel> getHighVoltageControlVoltageLevels() {
        return Collections.unmodifiableList(highVoltageControlVoltageLevels);
    }

    @Override
    public List<ControlVoltageLevel> getLowVoltageControlVoltageLevels() {
        return Collections.unmodifiableList(lowVoltageControlVoltageLevels);
    }

    @Override
    public Optional<ControlVoltageLevel> getHighVoltageControlVoltageLevel(String id) {
        Objects.requireNonNull(id);
        return highVoltageControlVoltageLevels.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<ControlVoltageLevel> getLowVoltageControlVoltageLevel(String id) {
        Objects.requireNonNull(id);
        return lowVoltageControlVoltageLevels.stream().filter(u -> u.getId().equals(id)).findFirst();
    }

}
