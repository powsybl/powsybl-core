/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class VscConverterStationImpl extends AbstractHvdcConverterStation<VscConverterStation> implements VscConverterStation, ReactiveLimitsOwner {

    static final String TYPE_DESCRIPTION = "vscConverterStation";

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private final TDoubleArrayList localTargetQ;

    private final TDoubleArrayList localTargetV;

    private VoltageRegulationExt voltageRegulation;

    VscConverterStationImpl(String id, String name, boolean fictitious, float lossFactor, Ref<NetworkImpl> ref,
                            double localTargetQ, double localTargetV, VoltageRegulationExt voltageRegulation) {
        super(ref, id, name, fictitious, lossFactor);
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.localTargetQ = new TDoubleArrayList(variantArraySize);
        this.localTargetV = new TDoubleArrayList(variantArraySize);
        this.localTargetQ.fill(0, variantArraySize, localTargetQ);
        this.localTargetV.fill(0, variantArraySize, localTargetV);
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.voltageRegulation = voltageRegulation;
        if (this.voltageRegulation != null) {
            this.voltageRegulation.updateValidable(this);
            this.voltageRegulation.setParent(this);
        }
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
        return isWithMode(RegulationMode.VOLTAGE);
    }

    @Override
    public VscConverterStationImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            boolean oldValue = regulation.setRegulating(voltageRegulatorOn);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        });
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return this.getRegulatingTargetV();
    }

    @Override
    public VscConverterStationImpl setVoltageSetpoint(double voltageSetpoint) {
        double oldValue;
        if (voltageRegulation != null && isWithMode(RegulationMode.VOLTAGE)) {
            oldValue = voltageRegulation.getTargetValue();
            voltageRegulation.setTargetValue(voltageSetpoint);
        } else {
            oldValue = this.getLocalTargetV();
            this.setLocalTargetV(voltageSetpoint);
        }
        String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
        notifyUpdate("voltageSetpoint", variantId, oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return this.getRegulatingTargetQ();
    }

    @Override
    public double getLocalTargetV() {
        return this.localTargetV.get(getNetwork().getVariantIndex());
    }

    @Override
    public VscConverterStation setLocalTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetV, "targetV");
        int variantIndex = n.getVariantIndex();
        double oldValue = this.localTargetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public VscConverterStation setLocalTargetQ(double targetQ) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkLocalTargetQandV(this, this.getLocalTargetV(), targetQ, getVoltageRegulation(), n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        double oldValue = this.localTargetQ.set(variantIndex, targetQ);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public double getLocalTargetQ() {
        return this.localTargetQ.get(getNetwork().getVariantIndex());
    }

    @Override
    public VscConverterStationImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        double oldValue;
        if (voltageRegulation != null && isWithMode(RegulationMode.REACTIVE_POWER)) {
            oldValue = voltageRegulation.getTargetValue();
            voltageRegulation.setTargetValue(reactivePowerSetpoint);
        } else {
            oldValue = this.getLocalTargetQ();
            this.setLocalTargetQ(reactivePowerSetpoint);
        }
        String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
        notifyUpdate("reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
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
        this.reactiveLimits.setReactiveLimits(reactiveLimits);
    }

    @Override
    public ReactiveLimits getReactiveLimits() {
        return reactiveLimits.getReactiveLimits();
    }

    @Override
    public <L extends ReactiveLimits> L getReactiveLimits(Class<L> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        localTargetQ.ensureCapacity(localTargetQ.size() + number);
        localTargetQ.fill(initVariantArraySize, initVariantArraySize + number, localTargetQ.get(sourceIndex));
        localTargetV.ensureCapacity(localTargetV.size() + number);
        localTargetV.fill(initVariantArraySize, initVariantArraySize + number, localTargetV.get(sourceIndex));
        getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        localTargetQ.remove(localTargetQ.size() - number, number);
        localTargetV.remove(localTargetV.size() - number, number);
        getOptionalVoltageRegulation().ifPresent(vr -> vr.reduceVariantArraySize(number));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            localTargetQ.set(index, localTargetQ.get(sourceIndex));
            localTargetV.set(index, localTargetV.get(sourceIndex));
        }
        getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl(VscConverterStation.class, this, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.setVoltageRegulation((VoltageRegulationExt) voltageRegulation);
        return this.voltageRegulation;
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationExt> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        ValidationUtil.checkLocalTargetQandV(this, this.getLocalTargetV(), this.getLocalTargetQ(), true, false, null, getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        this.voltageRegulation = null;
    }

    @Override
    public VscConverterStationImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            Terminal oldValue = regulation.getTerminal();
            double targetValue = isWithMode(RegulationMode.VOLTAGE) ? getRegulatingTargetV() : getRegulatingTargetQ();
            regulation.setTerminal(regulatingTerminal, targetValue);
            String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
            notifyUpdate("regulatingTerminal", variantId, oldValue, regulatingTerminal);
        });
        return this;
    }

    @Override
    public void remove() {
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        super.remove();
    }

    private void setVoltageRegulation(VoltageRegulationExt voltageRegulation) {
        getOptionalVoltageRegulation().ifPresent(VoltageRegulationExt::remove);
        this.voltageRegulation = voltageRegulation;
        this.attachVoltageRegulation(voltageRegulation, this);
    }
}
