/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TFloatArrayList;

import java.util.BitSet;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorImpl extends ConnectableImpl<Generator> implements Generator, ReactiveLimitsOwner {

    private EnergySource energySource;

    private float minP;

    private float maxP;

    private float ratedS;

    private ReactiveLimits reactiveLimits;

    private TerminalExt regulatingTerminal;

    // attributes depending on the state

    private final BitSet voltageRegulatorOn;

    private final TFloatArrayList targetP;

    private final TFloatArrayList targetQ;

    private final TFloatArrayList targetV;

    GeneratorImpl(Ref<? extends MultiStateObject> ref,
                  String id, String name, EnergySource energySource,
                  float minP, float maxP,
                  boolean voltageRegulatorOn, TerminalExt regulatingTerminal,
                  float targetP, float targetQ, float targetV,
                  float ratedS) {
        super(id, name);
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        reactiveLimits = new MinMaxReactiveLimitsImpl(-Float.MAX_VALUE, Float.MAX_VALUE);
        this.regulatingTerminal = regulatingTerminal;
        this.ratedS = ratedS;
        int stateArraySize = ref.get().getStateManager().getStateArraySize();
        this.voltageRegulatorOn = new BitSet(stateArraySize);
        this.targetP = new TFloatArrayList(stateArraySize);
        this.targetQ = new TFloatArrayList(stateArraySize);
        this.targetV = new TFloatArrayList(stateArraySize);
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
    public float getMaxP() {
        return maxP;
    }

    @Override
    public GeneratorImpl setMaxP(float maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        float oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    @Override
    public float getMinP() {
        return minP;
    }

    @Override
    public GeneratorImpl setMinP(float minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActiveLimits(this, minP, maxP);
        float oldValue = this.minP;
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
    public float getTargetP() {
        return targetP.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetP(float targetP) {
        ValidationUtil.checkTargetP(this, targetP);
        float oldValue = this.targetP.set(getNetwork().getStateIndex(), targetP);
        notifyUpdate("targetP", oldValue, targetP);
        return this;
    }

    @Override
    public float getTargetQ() {
        return targetQ.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetQ(float targetQ) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV.get(stateIndex), targetQ);
        float oldValue = this.targetQ.set(stateIndex, targetQ);
        notifyUpdate("targetQ", oldValue, targetQ);
        return this;
    }

    @Override
    public float getTargetV() {
        return this.targetV.get(getNetwork().getStateIndex());
    }

    @Override
    public GeneratorImpl setTargetV(float targetV) {
        int stateIndex = getNetwork().getStateIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(stateIndex), targetV, targetQ.get(stateIndex));
        float oldValue = this.targetV.set(stateIndex, targetV);
        notifyUpdate("targetV", oldValue, targetV);
        return this;
    }

    @Override
    public float getRatedS() {
        return ratedS;
    }

    @Override
    public GeneratorImpl setRatedS(float ratedS) {
        ValidationUtil.checkRatedS(this, ratedS);
        float oldValue = this.ratedS;
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
