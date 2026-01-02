/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.regulation.VoltageRegulationAdderImpl;
import com.powsybl.iidm.network.impl.regulation.VoltageRegulationImpl;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import gnu.trove.list.array.TDoubleArrayList;

import static com.powsybl.iidm.network.ValidationUtil.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class GeneratorImpl extends AbstractConnectable<Generator> implements Generator, ReactiveLimitsOwner {

    private final Ref<? extends VariantManagerHolder> network;

    private EnergySource energySource;

    private double minP;

    private double maxP;

    private double ratedS;

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private VoltageRegulationImpl voltageRegulation;

    // attributes depending on the variant
    private final TDoubleArrayList targetP;
    private final TDoubleArrayList equivalentLocalTargetQ;
    private final TDoubleArrayList equivalentLocalTargetV;

    private final boolean isCondenser;

    GeneratorImpl(Ref<NetworkImpl> network,
                  String id, String name, boolean fictitious, EnergySource energySource,
                  double minP, double maxP,
                  VoltageRegulationImpl voltageRegulation,
                  double targetP, double targetQ, double equivalentLocalTargetV,
                  double ratedS, boolean isCondenser) {
        super(network, id, name, fictitious);
        this.network = network;
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        this.voltageRegulation = voltageRegulation != null ? voltageRegulation : VoltageRegulationImpl.builder().setNetwork(network).build();
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.ratedS = ratedS;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.equivalentLocalTargetQ = new TDoubleArrayList(variantArraySize);
        this.equivalentLocalTargetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetP.add(targetP);
            this.equivalentLocalTargetQ.add(targetQ);
            this.equivalentLocalTargetV.add(equivalentLocalTargetV);
        }
        this.isCondenser = isCondenser;
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
        return this.voltageRegulation.isRegulating();
    }

    @Override
    public GeneratorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
//        checkVoltageRegulation(this.voltageRegulation);
        boolean oldValue = this.voltageRegulation.setRegulating(voltageRegulatorOn);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    @Deprecated(forRemoval = true)
    public TerminalExt getRegulatingTerminal() {
        TerminalExt regulatingTerminal = (TerminalExt) voltageRegulation.getTerminal();
        TerminalExt terminalExt = regulatingTerminal != null ? regulatingTerminal : getTerminal();
        return terminalExt;
    }

    @Override
    @Deprecated(forRemoval = true)
    public GeneratorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        Terminal oldValue = voltageRegulation.getTerminal();
        voltageRegulation.setTerminal(regulatingTerminal);
        notifyUpdate("regulatingTerminal", oldValue, regulatingTerminal);
        return this;
    }

    @Override
    public double getTargetP() {
        return targetP.get(network.get().getVariantIndex());
    }

    @Override
    public GeneratorImpl setTargetP(double targetP) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkActivePowerSetpoint(this, targetP, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.targetP.set(network.get().getVariantIndex(), targetP);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetP", variantId, oldValue, targetP);
        return this;
    }

    @Override
    public double getTargetQ() {
        if (RegulationMode.REACTIVE_POWER == this.voltageRegulation.getMode()) {
            return this.voltageRegulation.getTargetQ();
        }
        return this.getEquivalentLocalTargetQ();
    }

    @Override
    public GeneratorImpl setTargetQ(double targetQ) {
        this.voltageRegulation.setTargetQ(targetQ);
        if (RegulationMode.REACTIVE_POWER != this.voltageRegulation.getMode()) {
            this.equivalentLocalTargetQ.set(network.get().getVariantIndex(), targetQ);
        }
        return this;
    }

    @Override
    @Deprecated(forRemoval = true)
    public double getTargetV() {
        if (RegulationMode.VOLTAGE == this.voltageRegulation.getMode()) {
            return this.voltageRegulation.getTargetV();
        }
        return this.getEquivalentLocalTargetV();
    }

    @Override
    @Deprecated(forRemoval = true)
    public GeneratorImpl setTargetV(double targetV) {
        return this.setTargetV(targetV, Double.NaN);
    }

    @Override
    @Deprecated(forRemoval = true)
    public GeneratorImpl setTargetV(double targetV, double equivalentLocalTargetV) {
        this.voltageRegulation.setTargetV(targetV);
        checkEquivalentLocalTargetV(this, equivalentLocalTargetV);
        this.equivalentLocalTargetV.set(network.get().getVariantIndex(), equivalentLocalTargetV);
        return this;
    }

    @Override
    public double getEquivalentLocalTargetV() {
        return this.equivalentLocalTargetV.get(network.get().getVariantIndex());
    }

    @Override
    public void setEquivalentLocalTargetV(double newEquivalentLocalTargetV) {
        checkEquivalentLocalTargetV(this, newEquivalentLocalTargetV);
        this.equivalentLocalTargetV.set(network.get().getVariantIndex(), newEquivalentLocalTargetV);
    }

    @Override
    public double getEquivalentLocalTargetQ() {
        return this.equivalentLocalTargetQ.get(network.get().getVariantIndex());
    }

    @Override
    public void setEquivalentLocalTargetQ(double newEquivalentLocalTargetQ) {
        NetworkImpl n = getNetwork();
        checkEquivalentLocalTargetQ(this, newEquivalentLocalTargetQ, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        this.equivalentLocalTargetQ.set(network.get().getVariantIndex(), newEquivalentLocalTargetQ);
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
    public boolean isCondenser() {
        return isCondenser;
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
    public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    @Override
    public void remove() {
        this.voltageRegulation.remove();
        super.remove();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        targetP.ensureCapacity(targetP.size() + number);
        equivalentLocalTargetQ.ensureCapacity(equivalentLocalTargetQ.size() + number);
        equivalentLocalTargetV.ensureCapacity(equivalentLocalTargetV.size() + number);
        for (int i = 0; i < number; i++) {
            targetP.add(targetP.get(sourceIndex));
            equivalentLocalTargetV.add(equivalentLocalTargetV.get(sourceIndex));
            equivalentLocalTargetQ.add(equivalentLocalTargetQ.get(sourceIndex));
        }
        this.voltageRegulation.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetP.remove(targetP.size() - number, number);
        equivalentLocalTargetV.remove(equivalentLocalTargetV.size() - number, number);
        equivalentLocalTargetQ.remove(equivalentLocalTargetQ.size() - number, number);
        this.voltageRegulation.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        this.voltageRegulation.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetP.set(index, targetP.get(sourceIndex));
            equivalentLocalTargetV.set(index, equivalentLocalTargetV.get(sourceIndex));
            equivalentLocalTargetQ.set(index, equivalentLocalTargetQ.get(sourceIndex));
        }
        this.voltageRegulation.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }

    @Override
    public VoltageRegulationImpl getVoltageRegulation() {
        return voltageRegulation;
    }

    @Override
    public VoltageRegulationBuilder<Generator> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(this, getNetwork().getRef());
    }

    @Override
    public void setVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.voltageRegulation = (VoltageRegulationImpl) voltageRegulation;
    }

}
