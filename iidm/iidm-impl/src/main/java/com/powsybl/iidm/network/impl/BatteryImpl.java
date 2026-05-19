/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.regulation.*;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;

/**
 * {@inheritDoc}
 *
 * @author Ghiles Abdellah {@literal <ghiles.abdellah at rte-france.com>}
 */
public class BatteryImpl extends AbstractConnectable<Battery> implements Battery, ReactiveLimitsOwner {

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private final TDoubleArrayList targetP;

    private final TDoubleArrayList localTargetQ;

    private final TDoubleArrayList localTargetV;

    private double minP;

    private double maxP;

    private VoltageRegulationExt voltageRegulation;

    BatteryImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                double targetP, double localTargetQ, double localTargetV,
                VoltageRegulationExt voltageRegulation,
                double minP, double maxP) {
        super(ref, id, name, fictitious);
        this.minP = minP;
        this.maxP = maxP;
        this.voltageRegulation = voltageRegulation;
        if (this.voltageRegulation != null) {
            this.voltageRegulation.updateValidable(this);
            this.voltageRegulation.setParent(this);
        }
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));

        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.targetP = new TDoubleArrayList(variantArraySize);
        this.localTargetQ = new TDoubleArrayList(variantArraySize);
        this.localTargetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetP.add(targetP);
            this.localTargetQ.add(localTargetQ);
            this.localTargetV.add(localTargetV);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTypeDescription() {
        return "Battery";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTargetP() {
        return targetP.get(getNetwork().getVariantIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setTargetP(double targetP) {
        NetworkImpl network = getNetwork();
        ValidationUtil.checkP0(this, targetP, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        int variantIndex = network.getVariantIndex();
        double oldValue = this.targetP.set(variantIndex, targetP);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        network.invalidateValidationLevel();
        notifyUpdate("targetP", variantId, oldValue, targetP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLocalTargetQ() {
        return localTargetQ.get(getNetwork().getVariantIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setLocalTargetQ(double targetQ) {
        NetworkImpl network = getNetwork();
        ValidationUtil.checkQ0(this, targetQ, network.getMinValidationLevel(), network.getReportNodeContext().getReportNode());
        int variantIndex = network.getVariantIndex();
        double oldValue = this.localTargetQ.set(variantIndex, targetQ);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        network.invalidateValidationLevel();
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMinP() {
        return minP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setMinP(double minP) {
        ValidationUtil.checkMinP(this, minP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        double oldValue = this.minP;
        this.minP = minP;
        notifyUpdate("minP", oldValue, minP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaxP() {
        return maxP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Battery setMaxP(double maxP) {
        ValidationUtil.checkMaxP(this, maxP);
        ValidationUtil.checkActivePowerLimits(this, minP, maxP);
        double oldValue = this.maxP;
        this.maxP = maxP;
        notifyUpdate("maxP", oldValue, maxP);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TerminalExt getTerminal() {
        return terminals.getFirst();
    }

    @Override
    public Battery setLocalTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        double oldValueLocalTargetV = this.localTargetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("localTargetV", variantId, oldValueLocalTargetV, targetV);
        return this;
    }

    @Override
    public double getLocalTargetV() {
        return this.localTargetV.get(getNetwork().getVariantIndex());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setReactiveLimits(ReactiveLimits reactiveLimits) {
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R extends ReactiveLimits> R getReactiveLimits(Class<R> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReactiveCapabilityCurveAdder newReactiveCapabilityCurve() {
        return new ReactiveCapabilityCurveAdderImpl<>(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MinMaxReactiveLimitsAdder newMinMaxReactiveLimits() {
        return new MinMaxReactiveLimitsAdderImpl<>(this);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetP.remove(targetP.size() - number, number);
        localTargetQ.remove(localTargetQ.size() - number, number);
        localTargetV.remove(localTargetV.size() - number, number);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.reduceVariantArraySize(number));
    }

    /**
     * {@inheritDoc}
     */
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
    public void remove() {
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        super.remove();
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationExt> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(Battery.class, this, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.setVoltageRegulation((VoltageRegulationExt) voltageRegulation);
        return this.voltageRegulation;
    }

    @Override
    public void removeVoltageRegulation() {
        ValidationUtil.checkLocalTargetQandV(this, this.getLocalTargetV(), this.getLocalTargetQ(), true, false, null, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        this.voltageRegulation = null;
    }

    private void setVoltageRegulation(VoltageRegulationExt voltageRegulation) {
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        this.voltageRegulation = voltageRegulation;
        this.attachVoltageRegulation(voltageRegulation, this);
    }
}
