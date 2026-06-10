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

import javax.annotation.Nonnull;
import java.util.Optional;

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

    private VoltageRegulationExt voltageRegulation;

    // attributes depending on the variant

    private final TDoubleArrayList targetP;

    private final TDoubleArrayList localTargetQ;

    private final TDoubleArrayList localTargetV;

    private final boolean isCondenser;

    GeneratorImpl(Ref<NetworkImpl> network,
                  String id, String name, boolean fictitious, EnergySource energySource,
                  double minP, double maxP,
                  VoltageRegulationExt voltageRegulation,
                  double targetP, double localTargetQ, double localTargetV,
                  double ratedS, boolean isCondenser) {
        super(network, id, name, fictitious);
        this.network = network;
        this.energySource = energySource;
        this.minP = minP;
        this.maxP = maxP;
        this.voltageRegulation = voltageRegulation;
        this.attachVoltageRegulation();
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.ratedS = ratedS;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.localTargetQ = new TDoubleArrayList(variantArraySize);
        this.localTargetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetP.add(targetP);
            this.localTargetQ.add(localTargetQ);
            this.localTargetV.add(localTargetV);
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
        return this.isRegulatingWithMode(RegulationMode.VOLTAGE);
    }

    @Override
    public GeneratorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        if (voltageRegulation != null) {
            boolean oldValue = voltageRegulation.isRegulating();
            voltageRegulation.setRegulating(voltageRegulatorOn);
            String variantId = network.get().getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(voltageRegulatorOn)
                .build();
        }
        return this;
    }

    @Override
    public GeneratorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        if (voltageRegulation != null) {
            Terminal oldValue = voltageRegulation.getTerminal();
            double targetValue = isWithMode(RegulationMode.VOLTAGE) ? getRegulatingTargetV() : getRegulatingTargetQ();
            voltageRegulation.setTerminal(regulatingTerminal, targetValue);
            notifyUpdate("regulatingTerminal", oldValue, regulatingTerminal);
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTerminal(regulatingTerminal)
                .withRegulating(false)
                .build();
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
        return this.getRegulatingTargetQ();
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ.get(network.get().getVariantIndex());
    }

    @Override
    public GeneratorImpl setLocalTargetQ(double localTargetQ) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.localTargetQ.set(variantIndex, localTargetQ);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("localTargetQ", variantId, oldValue, localTargetQ);
        return this;
    }

    @Override
    public GeneratorImpl setTargetQ(double targetQ) {
        return this.setLocalTargetQ(targetQ);
    }

    @Override
    public double getTargetV() {
        return this.getRegulatingTargetV();
    }

    @Override
    public GeneratorImpl setLocalTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValueLocalTargetV = this.localTargetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("localTargetV", variantId, oldValueLocalTargetV, targetV);
        return this;
    }

    @Override
    public double getLocalTargetV() {
        return this.localTargetV.get(getCurrentIndex());
    }

    @Override
    public GeneratorImpl setTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetV, "targetV");
        int variantIndex = n.getVariantIndex();
        double oldValueTargetV = this.localTargetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetV", variantId, oldValueTargetV, targetV);
        return this;
    }

    @Override
    public GeneratorImpl setTargetV(double targetV, double equivalentLocalTargetV) {
        if (voltageRegulation != null) {
            if (isRemoteRegulating() && isWithMode(RegulationMode.VOLTAGE)) {
                setLocalTargetV(equivalentLocalTargetV);
                getVoltageRegulation().setTargetValue(targetV);
            } else {
                setLocalTargetV(targetV);
            }
        } else {
            newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withTargetValue(targetV)
                .withRegulating(false)
                .build();
            setLocalTargetV(equivalentLocalTargetV);
        }
        return this;
    }

    @Override
    public double getEquivalentLocalTargetV() {
        return isRemoteRegulating() ? this.getLocalTargetV() : Double.NaN;
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
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::onRemove);
        super.remove();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        targetP.ensureCapacity(targetP.size() + number);
        localTargetQ.ensureCapacity(localTargetQ.size() + number);
        localTargetV.ensureCapacity(localTargetV.size() + number);
        for (int i = 0; i < number; i++) {
            targetP.add(targetP.get(sourceIndex));
            localTargetQ.add(localTargetQ.get(sourceIndex));
            localTargetV.add(localTargetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetP.remove(targetP.size() - number, number);
        localTargetQ.remove(localTargetQ.size() - number, number);
        localTargetV.remove(localTargetV.size() - number, number);
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
            localTargetQ.set(index, localTargetQ.get(sourceIndex));
            localTargetV.set(index, localTargetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    protected String getTypeDescription() {
        return "Generator";
    }

    @Override
    public VoltageRegulationExt getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationExt> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(Generator.class, this, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        ValidationUtil.checkLocalTargetQandV(this, this.getLocalTargetV(), this.getLocalTargetQ(), true, false, null, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::onRemove);
        this.voltageRegulation = null;
    }

    /**
     * Creates or updates the voltage regulation associated with this battery.
     * <p>
     * If a voltage regulation already exists, only the current variant attributes are updated from the
     * provided voltage regulation, while keeping the existing instance.
     * </p>
     * <p>
     * This method must remain private to ensure voltage regulation lifecycle operations are done through
     * the public API and to avoid sharing a voltage regulation instance between equipments.
     * </p>
     *
     * @param voltageRegulation the voltage regulation to attach or use as source attributes
     * @return the voltage regulation associated with this equipment
     */
    private VoltageRegulationExt setVoltageRegulation(@Nonnull VoltageRegulationExt voltageRegulation) {
        if (this.voltageRegulation == null) {
            this.voltageRegulation = voltageRegulation;
        } else {
            this.voltageRegulation.setAttributesOnCurrentVariant(voltageRegulation);
        }
        this.attachVoltageRegulation();
        return this.voltageRegulation;
    }

    private int getCurrentIndex() {
        return network.get().getVariantIndex();
    }

    private void attachVoltageRegulation() {
        getOptionalVoltageRegulation().ifPresent(vr -> {
            vr.updateValidable(this);
            vr.setHolder(this);
        });
    }
}
