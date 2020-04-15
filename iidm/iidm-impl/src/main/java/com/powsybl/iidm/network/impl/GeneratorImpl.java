/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class GeneratorImpl extends AbstractConnectable<Generator> implements Generator, ReactiveLimitsOwner {

    private EnergySource energySource;

    private double minP;

    private double maxP;

    private double ratedS;

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    private final TBooleanArrayList voltageRegulatorOn;

    private final TDoubleArrayList targetP;

    private final TDoubleArrayList targetQ;

    private final TDoubleArrayList targetV;

    GeneratorImpl(Ref<? extends VariantManagerHolder> ref,
                  String id, String name, boolean fictitious, EnergySource energySource,
                  double minP, double maxP,
                  boolean voltageRegulatorOn, TerminalExt regulatingTerminal,
                  double targetP, double targetQ, double targetV,
                  double ratedS) {
        super(id, name, fictitious);
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.regulatingTerminal = regulatingTerminal;
        this.ratedS = ratedS;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.voltageRegulatorOn = new TBooleanArrayList(variantArraySize);
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.targetQ = new TDoubleArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.voltageRegulatorOn.add(voltageRegulatorOn);
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
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
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
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        double oldValue = this.minP;
        this.minP = minP;
        notifyUpdate("minP", oldValue, minP);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(getNetwork().getVariantIndex());
    }

    @Override
    public GeneratorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(variantIndex), targetQ.get(variantIndex));
        boolean oldValue = this.voltageRegulatorOn.set(variantIndex, voltageRegulatorOn);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public GeneratorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        Terminal oldValue = this.regulatingTerminal;
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        notifyUpdate("regulatingTerminal", oldValue, regulatingTerminal);
        return this;
    }

    @Override
    public double getTargetP() {
        return targetP.get(getNetwork().getVariantIndex());
    }

    @Override
    public GeneratorImpl setTargetP(double targetP) {
        ValidationUtil.checkActivePowerSetpoint(this, targetP);
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.targetP.set(getNetwork().getVariantIndex(), targetP);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetP", variantId, oldValue, targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        return targetQ.get(getNetwork().getVariantIndex());
    }

    @Override
    public GeneratorImpl setTargetQ(double targetQ) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(variantIndex), targetV.get(variantIndex), targetQ);
        double oldValue = this.targetQ.set(variantIndex, targetQ);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public double getTargetV() {
        return this.targetV.get(getNetwork().getVariantIndex());
    }

    @Override
    public GeneratorImpl setTargetV(double targetV) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(variantIndex), targetV, targetQ.get(variantIndex));
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetV", variantId, oldValue, targetV);
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
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    @Override
    public <RL extends ReactiveLimits> RL getReactiveLimits(Class<RL> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        voltageRegulatorOn.ensureCapacity(voltageRegulatorOn.size() + number);
        targetP.ensureCapacity(targetP.size() + number);
        targetQ.ensureCapacity(targetQ.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; i++) {
            voltageRegulatorOn.add(voltageRegulatorOn.get(sourceIndex));
            targetP.add(targetP.get(sourceIndex));
            targetQ.add(targetQ.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        voltageRegulatorOn.remove(voltageRegulatorOn.size() - number, number);
        targetP.remove(targetP.size() - number, number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
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
