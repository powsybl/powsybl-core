/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.BitSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorImpl extends AbstractConnectable<Generator> implements Generator, ReactiveLimitsOwner {

    private EnergySource energySource;

    private double minP;

    private double maxP;

    private double ratedS;

    private ReactiveLimits reactiveLimits;

    private TerminalExt regulatingTerminal;

    // attributes depending on the state

    private final BitSet voltageRegulatorOn;

    private final TDoubleArrayList targetP;

    private final TDoubleArrayList targetQ;

    private final TDoubleArrayList targetV;

    GeneratorImpl(Ref<? extends MultiStateObject> ref,
                  String id, String name, EnergySource energySource,
                  double minP, double maxP,
                  boolean voltageRegulatorOn, TerminalExt regulatingTerminal,
                  double targetP, double targetQ, double targetV,
                  double ratedS) {
        super(id, name);
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        reactiveLimits = new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE);
        this.regulatingTerminal = regulatingTerminal;
        this.ratedS = ratedS;
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageRegulatorOn = new BitSet(stateArraySize);
        this.targetP = new TDoubleArrayList(stateArraySize);
        this.targetQ = new TDoubleArrayList(stateArraySize);
        this.targetV = new TDoubleArrayList(stateArraySize);
        this.voltageRegulatorOn.set(0, stateArraySize, voltageRegulatorOn);
        for (int i = 0; i < stateArraySize; i++) {
            this.targetP.add(targetP);
            this.targetQ.add(targetQ);
            this.targetV.add(targetV);
        }
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.GENERATOR;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public EnergySource getEnergySource() {
        return energySource;
    }

    @Override
    public GeneratorImpl setEnergySource(EnergySource energySource) {
        ValidationUtil.checkEnergySource(this, energySource);
        EnergySource oldValue = this.energySource;
        this.energySource = energySource;
        notifyUpdate("energySource", oldValue.toString(), energySource.toString());
        return this;
    }

    @Override
    public double getMaxP() {
        return maxP;
    }

    @Override
    public GeneratorImpl setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public double getMinP() {
        return minP;
    }

    @Override
    public GeneratorImpl setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        double oldValue = this.minP;
        this.minP = minP;
        notifyUpdate("minP", oldValue, minP);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(stateIndex), targetQ.get(stateIndex));
        boolean oldValue = this.voltageRegulatorOn.get(stateIndex);
        this.voltageRegulatorOn.set(stateIndex, voltageRegulatorOn);
        notifyUpdate("voltageRegulatorOn", oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public GeneratorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, (TerminalExt) regulatingTerminal, getNetwork());
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        return this;
    }

    @Override
    public double getTargetP() {
        return targetP.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetP(double targetP) {
        ValidationUtil.checkActivePowerSetpoint(this, targetP);
        double oldValue = this.targetP.set(getNetwork().getStateIndex(), targetP);
        notifyUpdate("targetP", oldValue, targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return targetQ.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetQ(double targetQ) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV.get(stateIndex), targetQ);
        double oldValue = this.targetQ.set(stateIndex, targetQ);
        notifyUpdate("targetQ", oldValue, targetQ);
        return this;
    }

    @Override
    public double getTargetV() {
        return this.targetV.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetV(double targetV) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV, targetQ.get(stateIndex));
        double oldValue = this.targetV.set(stateIndex, targetV);
        notifyUpdate("targetV", oldValue, targetV);
        return this;
    }

    @Override
    public double getRatedS() {
        return ratedS;
    }

    @Override
    public GeneratorImpl setRatedS(double ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        double oldValue = this.ratedS;
        this.ratedS = ratedS;
        notifyUpdate("ratedS", oldValue, ratedS);
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
        targetP.ensureCapacity(targetP.size() + number);
        targetQ.ensureCapacity(targetQ.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; i++) {
            voltageRegulatorOn.set(initStateArraySize + i, voltageRegulatorOn.get(sourceIndex));
            targetP.add(targetP.get(sourceIndex));
            targetQ.add(targetQ.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        targetP.remove(targetP.size() - number, number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
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
            targetP.set(index, targetP.get(sourceIndex));
            targetQ.set(index, targetQ.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }

}
