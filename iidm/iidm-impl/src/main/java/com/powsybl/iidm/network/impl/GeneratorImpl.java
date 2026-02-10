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
import com.powsybl.iidm.network.regulation.*;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;
import java.util.Set;

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

    private final TDoubleArrayList targetQ;

    private final TDoubleArrayList targetV;

    private final TDoubleArrayList equivalentLocalTargetV;

    private final boolean isCondenser;

    GeneratorImpl(Ref<NetworkImpl> network,
                  String id, String name, boolean fictitious, EnergySource energySource,
                  double minP, double maxP,
                  VoltageRegulation voltageRegulation,
                  double targetP, double targetQ, double targetV,
                  double ratedS, boolean isCondenser, double equivalentLocalTargetV) {
        super(network, id, name, fictitious);
        this.network = network;
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        this.voltageRegulation = (VoltageRegulationImpl) voltageRegulation;
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.ratedS = ratedS;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.targetQ = new TDoubleArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.equivalentLocalTargetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetP.add(targetP);
            this.targetQ.add(targetQ);
            this.targetV.add(targetV);
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
    @Deprecated(forRemoval = true, since = "7.2.0")
    public boolean isVoltageRegulatorOn() {
        return this.voltageRegulation != null
            && RegulationMode.VOLTAGE == this.voltageRegulation.getMode()
            && this.voltageRegulation.isRegulating();
    }

    @Override
    public GeneratorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this,
            voltageRegulatorOn, targetV.get(variantIndex), targetQ.get(variantIndex),
            n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        if (this.voltageRegulation != null) {
            boolean oldValue = this.voltageRegulation.setRegulating(true);
            if (voltageRegulatorOn) {
                this.voltageRegulation.setMode(RegulationMode.VOLTAGE);
            } else {
                this.voltageRegulation.setMode(RegulationMode.REACTIVE_POWER);
            }
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        } else {
            throw new IllegalStateException(); // TODO MSA
        }
        return this;
    }

    @Override
    @Deprecated(forRemoval = true)
    public TerminalExt getRegulatingTerminal() {
        TerminalExt regulatingTerminal = voltageRegulation != null ? (TerminalExt) voltageRegulation.getTerminal() : null;
        TerminalExt terminalExt = regulatingTerminal != null ? regulatingTerminal : getTerminal();
        return terminalExt;
    }

    @Override
    @Deprecated(forRemoval = true)
    public GeneratorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        if (voltageRegulation != null) {
            Terminal oldValue = voltageRegulation.getTerminal();
            voltageRegulation.setTerminal(regulatingTerminal);
            notifyUpdate("regulatingTerminal", oldValue, regulatingTerminal);
        }
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
        return targetQ.get(network.get().getVariantIndex());
    }

    @Override
    public GeneratorImpl setTargetQ(double targetQ) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
//        TODO MSA Check if we need this validation
//        ValidationUtil.checkVoltageControl(this, this.isVoltageRegulatorOn(),
//                this.getTargetV(), targetQ, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.targetQ.set(variantIndex, targetQ);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public double getRemoteTargetV() {
        if (this.voltageRegulation != null && Boolean.TRUE.equals(this.voltageRegulation.isRegulating()) && RegulationMode.VOLTAGE == this.voltageRegulation.getMode()) {
            return this.voltageRegulation.getTargetValue();
        }
        return Double.NaN;
    }

    @Override
    public double getTargetV() {
        return this.targetV.get(network.get().getVariantIndex());
    }

    @Override
    @Deprecated(forRemoval = true, since = "7.2.0")
    public GeneratorImpl setTargetV(double targetV) {
        return this.setTargetV(targetV, Double.NaN);
    }

    @Override
    public GeneratorImpl setTargetV(double targetV, double equivalentLocalTargetV) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, this.isVoltageRegulatorOn(),
            targetV, targetQ.get(variantIndex), n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        ValidationUtil.checkEquivalentLocalTargetV(this, equivalentLocalTargetV);
        double oldValueTargetV = this.targetV.set(variantIndex, targetV);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetV", variantId, oldValueTargetV, targetV);
        this.equivalentLocalTargetV.set(variantIndex, equivalentLocalTargetV);
        if (this.voltageRegulation != null) {
//            this.voltageRegulation.setTargetValue(targetV);
        }
        return this;
    }

    @Override
    public double getEquivalentLocalTargetV() {
        return this.equivalentLocalTargetV.get(network.get().getVariantIndex());
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
        targetQ.ensureCapacity(targetQ.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        equivalentLocalTargetV.ensureCapacity(equivalentLocalTargetV.size() + number);
        for (int i = 0; i < number; i++) {
            targetP.add(targetP.get(sourceIndex));
            targetQ.add(targetQ.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
            equivalentLocalTargetV.add(equivalentLocalTargetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetP.remove(targetP.size() - number, number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
        equivalentLocalTargetV.remove(equivalentLocalTargetV.size() - number, number);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.reduceVariantArraySize(number));
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.deleteVariantArrayElement(index));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetP.set(index, targetP.get(sourceIndex));
            targetQ.set(index, targetQ.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
            equivalentLocalTargetV.set(index, equivalentLocalTargetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }

    @Override
    public VoltageRegulationImpl getVoltageRegulation() {
        return voltageRegulation;
    }

    private Optional<VoltageRegulationImpl> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl<>(this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        this.voltageRegulation = null;
    }

    @Override
    public Set<RegulationMode> getAllowedRegulationModes() {
        return RegulationMode.getAllowedRegulationModes(Generator.class);
    }

    public void setVoltageRegulation(VoltageRegulationImpl voltageRegulation) {
        this.voltageRegulation = voltageRegulation;
    }
}
