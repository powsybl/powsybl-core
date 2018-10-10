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

    private final TDoubleArrayList voltageSetpoint;

    private final TDoubleArrayList reactivePowerSetpoint;

    private final TIntArrayList regulationMode;

    StaticVarCompensatorImpl(String id, String name, double bMin, double bMax, double voltageSetpoint, double reactivePowerSetpoint,
                             RegulationMode regulationMode, Ref<? extends MultiStateObject> ref) {
        super(id, name);
        this.bMin = bMin;
        this.bMax = bMax;
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageSetpoint = new TDoubleArrayList(stateArraySize);
        this.reactivePowerSetpoint = new TDoubleArrayList(stateArraySize);
        this.regulationMode = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.voltageSetpoint.add(voltageSetpoint);
            this.reactivePowerSetpoint.add(reactivePowerSetpoint);
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
    public double getVoltageSetpoint() {
        return voltageSetpoint.get(getNetwork().getStateIndex());
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetpoint(double voltageSetpoint) {
        ValidationUtil.checkSvcRegulator(this, voltageSetpoint, getReactivePowerSetpoint(), getRegulationMode());
        double oldValue = this.voltageSetpoint.set(getNetwork().getStateIndex(), voltageSetpoint);
        notifyUpdate("voltageSetpoint", oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return reactivePowerSetpoint.get(getNetwork().getStateIndex());
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), reactivePowerSetpoint, getRegulationMode());
        double oldValue = this.reactivePowerSetpoint.set(getNetwork().getStateIndex(), reactivePowerSetpoint);
        notifyUpdate("reactivePowerSetpoint", oldValue, reactivePowerSetpoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.values()[regulationMode.get(getNetwork().getStateIndex())];
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetpoint(), getReactivePowerSetpoint(), regulationMode);
        RegulationMode oldValue = RegulationMode.values()[this.regulationMode.set(getNetwork().getStateIndex(), regulationMode.ordinal())];
        notifyUpdate("regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        voltageSetpoint.ensureCapacity(voltageSetpoint.size() + number);
        reactivePowerSetpoint.ensureCapacity(reactivePowerSetpoint.size() + number);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        for (int i = 0; i < number; i++) {
            voltageSetpoint.add(voltageSetpoint.get(sourceIndex));
            reactivePowerSetpoint.add(reactivePowerSetpoint.get(sourceIndex));
            regulationMode.add(regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        voltageSetpoint.remove(voltageSetpoint.size() - number, number);
        reactivePowerSetpoint.remove(reactivePowerSetpoint.size() - number, number);
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
            voltageSetpoint.set(index, voltageSetpoint.get(sourceIndex));
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.get(sourceIndex));
            regulationMode.set(index, regulationMode.get(sourceIndex));
        }
    }

}
