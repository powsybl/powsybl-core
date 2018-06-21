/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StaticVarCompensatorImpl extends AbstractConnectable<StaticVarCompensator> implements StaticVarCompensator {

    static final String TYPE_DESCRIPTION = "staticVarCompensator";

    private double bMin;

    private double bMax;

    // attributes depending on the state

    private final TDoubleArrayList voltageSetPoint;

    private final TDoubleArrayList reactivePowerSetPoint;

    private final TIntArrayList regulationMode;

    StaticVarCompensatorImpl(String id, String name, double bMin, double bMax, double voltageSetPoint, double reactivePowerSetPoint,
                             RegulationMode regulationMode, Ref<? extends MultiStateObject> ref) {
        super(id, name);
        this.bMin = bMin;
        this.bMax = bMax;
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageSetPoint = new TDoubleArrayList(stateArraySize);
        this.reactivePowerSetPoint = new TDoubleArrayList(stateArraySize);
        this.regulationMode = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.voltageSetPoint.add(voltageSetPoint);
            this.reactivePowerSetPoint.add(reactivePowerSetPoint);
            this.regulationMode.add(regulationMode.ordinal());
        }
    }

    @Override
    public Terminal getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.STATIC_VAR_COMPENSATOR;
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public double getBmin() {
        return bMin;
    }

    @Override
    public StaticVarCompensatorImpl setBmin(double bMin) {
        ValidationUtil.checkBmin(this, bMin);
        this.bMin = bMin;
        return this;
    }

    @Override
    public double getBmax() {
        return bMax;
    }

    @Override
    public StaticVarCompensatorImpl setBmax(double bMax) {
        ValidationUtil.checkBmax(this, bMax);
        this.bMax = bMax;
        return this;
    }

    @Override
    public double getVoltageSetPoint() {
        return voltageSetPoint.get(getNetwork().getStateIndex());
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetPoint(double voltageSetPoint) {
        ValidationUtil.checkSvcRegulator(this, voltageSetPoint, getReactivePowerSetPoint(), getRegulationMode());
        double oldValue = this.voltageSetPoint.set(getNetwork().getStateIndex(), voltageSetPoint);
        notifyUpdate("voltageSetPoint", oldValue, voltageSetPoint);
        return this;
    }

    @Override
    public double getReactivePowerSetPoint() {
        return reactivePowerSetPoint.get(getNetwork().getStateIndex());
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetPoint(double reactivePowerSetPoint) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetPoint(), reactivePowerSetPoint, getRegulationMode());
        double oldValue = this.reactivePowerSetPoint.set(getNetwork().getStateIndex(), reactivePowerSetPoint);
        notifyUpdate("reactivePowerSetPoint", oldValue, reactivePowerSetPoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.values()[regulationMode.get(getNetwork().getStateIndex())];
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetPoint(), getReactivePowerSetPoint(), regulationMode);
        RegulationMode oldValue = RegulationMode.values()[this.regulationMode.set(getNetwork().getStateIndex(), regulationMode.ordinal())];
        notifyUpdate("regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        voltageSetPoint.ensureCapacity(voltageSetPoint.size() + number);
        reactivePowerSetPoint.ensureCapacity(reactivePowerSetPoint.size() + number);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        for (int i = 0; i < number; i++) {
            voltageSetPoint.add(voltageSetPoint.get(sourceIndex));
            reactivePowerSetPoint.add(reactivePowerSetPoint.get(sourceIndex));
            regulationMode.add(regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        voltageSetPoint.remove(voltageSetPoint.size() - number, number);
        reactivePowerSetPoint.remove(reactivePowerSetPoint.size() - number, number);
        regulationMode.remove(regulationMode.size() - number, number);
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
            voltageSetPoint.set(index, voltageSetPoint.get(sourceIndex));
            reactivePowerSetPoint.set(index, reactivePowerSetPoint.get(sourceIndex));
            regulationMode.set(index, regulationMode.get(sourceIndex));
        }
    }

}
