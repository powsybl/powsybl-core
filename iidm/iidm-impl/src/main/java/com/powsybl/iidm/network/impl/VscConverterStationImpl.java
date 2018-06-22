/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.BitSet;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationImpl extends AbstractHvdcConverterStation<VscConverterStation> implements VscConverterStation, ReactiveLimitsOwner {

    static final String TYPE_DESCRIPTION = "vscConverterStation";

    private ReactiveLimits reactiveLimits;

    private final BitSet voltageRegulatorOn;

    private final TDoubleArrayList reactivePowerSetpoint;

    private final TDoubleArrayList voltageSetpoint;

    VscConverterStationImpl(String id, String name, float lossFactor, Ref<? extends MultiStateObject> ref,
                            boolean voltageRegulatorOn, double reactivePowerSetpoint, double voltageSetpoint) {
        super(id, name, lossFactor);
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageRegulatorOn = new BitSet(stateArraySize);
        this.reactivePowerSetpoint = new TDoubleArrayList(stateArraySize);
        this.voltageSetpoint = new TDoubleArrayList(stateArraySize);
        this.voltageRegulatorOn.set(0, stateArraySize, voltageRegulatorOn);
        this.reactivePowerSetpoint.fill(0, stateArraySize, reactivePowerSetpoint);
        this.voltageSetpoint.fill(0, stateArraySize, voltageSetpoint);
        this.reactiveLimits = new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE);
    }

    @Override
    public HvdcType getHvdcType() {
        return HvdcType.VSC;
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetpoint.get(stateIndex), reactivePowerSetpoint.get(stateIndex));
        boolean oldValue = this.voltageRegulatorOn.get(stateIndex);
        this.voltageRegulatorOn.set(stateIndex, voltageRegulatorOn);
        notifyUpdate("voltageRegulatorOn", oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return this.voltageSetpoint.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageSetpoint(double voltageSetpoint) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), voltageSetpoint, reactivePowerSetpoint.get(stateIndex));
        double oldValue = this.voltageSetpoint.set(stateIndex, voltageSetpoint);
        notifyUpdate("voltageSetpoint", oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return reactivePowerSetpoint.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), voltageSetpoint.get(stateIndex), reactivePowerSetpoint);
        double oldValue = this.reactivePowerSetpoint.set(stateIndex, reactivePowerSetpoint);
        notifyUpdate("reactivePowerSetpoint", oldValue, reactivePowerSetpoint);
        return this;
    }

    @Override
    public ReactiveCapabilityCurveAdderImpl newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl(this);
    }

    @Override
    public MinMaxReactiveLimitsAdderImpl newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl(this);
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits = reactiveLimits;
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits;
    }

    @Override
    public <RL extends ReactiveLimits> RL getReactiveLimits(Class<RL> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(reactiveLimits)) {
            return type.cast(reactiveLimits);
        } else {
            throw new ValidationException(this, "incorrect reactive limits type "
                    + type.getName() + ", expected " + reactiveLimits.getClass());
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);

        reactivePowerSetpoint.ensureCapacity(reactivePowerSetpoint.size() + number);
        reactivePowerSetpoint.fill(initStateArraySize, initStateArraySize + number, reactivePowerSetpoint.get(sourceIndex));

        voltageSetpoint.ensureCapacity(voltageSetpoint.size() + number);
        voltageSetpoint.fill(initStateArraySize, initStateArraySize + number, voltageSetpoint.get(sourceIndex));

        voltageRegulatorOn.set(initStateArraySize, initStateArraySize + number, voltageRegulatorOn.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        reactivePowerSetpoint.remove(reactivePowerSetpoint.size() - number, number);
        voltageSetpoint.remove(voltageSetpoint.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            voltageRegulatorOn.set(index, voltageRegulatorOn.get(sourceIndex));
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.get(sourceIndex));
            voltageSetpoint.set(index, voltageSetpoint.get(sourceIndex));
        }
    }
}
