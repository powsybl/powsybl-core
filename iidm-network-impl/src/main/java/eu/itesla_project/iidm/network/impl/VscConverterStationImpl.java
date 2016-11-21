/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ReactiveLimits;
import eu.itesla_project.iidm.network.VscConverterStation;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;

import java.util.BitSet;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationImpl extends HvdcConverterStationImpl<VscConverterStation> implements VscConverterStation, ReactiveLimitsOwner {

    static final String TYPE_DESCRIPTION = "vscConverterStation";

    private ReactiveLimits reactiveLimits;

    private final BitSet voltageRegulatorOn;

    private final TFloatArrayList reactivePowerSetPoint;

    private final TFloatArrayList voltageSetPoint;

    VscConverterStationImpl(String id, String name, Ref<? extends MultiStateObject> ref, boolean voltageRegulatorOn,
                            float reactivePowerSetPoint, float voltageSetPoint) {
        super(id, name);
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageRegulatorOn = new BitSet(stateArraySize);
        this.reactivePowerSetPoint = new TFloatArrayList(stateArraySize);
        this.voltageSetPoint = new TFloatArrayList(stateArraySize);
        this.voltageRegulatorOn.set(0, stateArraySize, voltageRegulatorOn);
        this.reactivePowerSetPoint.fill(0, stateArraySize, reactivePowerSetPoint);
        this.voltageSetPoint.fill(0, stateArraySize, voltageSetPoint);
        this.reactiveLimits = new MinMaxReactiveLimitsImpl(-Float.MAX_VALUE, Float.MAX_VALUE);
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
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetPoint.get(stateIndex), reactivePowerSetPoint.get(stateIndex));
        boolean oldValue = this.voltageRegulatorOn.get(stateIndex);
        this.voltageRegulatorOn.set(stateIndex, voltageRegulatorOn);
        notifyUpdate("voltageRegulatorOn", oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public float getVoltageSetPoint() {
        return this.voltageSetPoint.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageSetPoint(float targetV) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV, reactivePowerSetPoint.get(stateIndex));
        float oldValue = this.voltageSetPoint.set(stateIndex, targetV);
        notifyUpdate("voltageSetPoint", oldValue, targetV);
        return this;
    }

    @Override
    public float getReactivePowerSetPoint() {
        return reactivePowerSetPoint.get(getNetwork().getStateIndex());
    }

    @Override
    public VscConverterStationImpl setReactivePowerSetPoint(float targetQ) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), voltageSetPoint.get(stateIndex), targetQ);
        float oldValue = this.reactivePowerSetPoint.set(stateIndex, targetQ);
        notifyUpdate("reactivePowerSetPoint", oldValue, targetQ);
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

        reactivePowerSetPoint.ensureCapacity(reactivePowerSetPoint.size() + number);
        reactivePowerSetPoint.fill(initStateArraySize, initStateArraySize + number, reactivePowerSetPoint.get(sourceIndex));

        voltageSetPoint.ensureCapacity(voltageSetPoint.size() + number);
        voltageSetPoint.fill(initStateArraySize, initStateArraySize + number, voltageSetPoint.get(sourceIndex));

        voltageRegulatorOn.set(initStateArraySize, initStateArraySize + number, voltageRegulatorOn.get(sourceIndex));
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        reactivePowerSetPoint.remove(reactivePowerSetPoint.size() - number, number);
        voltageSetPoint.remove(voltageSetPoint.size() - number, number);
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
            reactivePowerSetPoint.set(index, reactivePowerSetPoint.get(sourceIndex));
            voltageSetPoint.set(index, voltageSetPoint.get(sourceIndex));
        }
    }
}
