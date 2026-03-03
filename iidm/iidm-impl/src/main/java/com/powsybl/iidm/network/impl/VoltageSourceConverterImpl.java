/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.regulation.VoltageRegulationBuilder;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class VoltageSourceConverterImpl extends AbstractAcDcConverter<VoltageSourceConverter> implements VoltageSourceConverter, ReactiveLimitsOwner {

    public static final String VOLTAGE_REGULATOR_ON_ATTRIBUTE = "voltageRegulatorOn";
    public static final String VOLTAGE_SETPOINT_ATTRIBUTE = "voltageSetpoint";
    public static final String REACTIVE_POWER_SETPOINT_ATTRIBUTE = "reactivePowerSetpoint";

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private final TDoubleArrayList targetQ;

    private final TDoubleArrayList targetV;

    private VoltageRegulationImpl voltageRegulation;

    VoltageSourceConverterImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                               double idleLoss, double switchingLoss, double resistiveLoss,
                               TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc,
                               double targetQ, double targetV, VoltageRegulationImpl voltageRegulation) {
        super(ref, id, name, fictitious, idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.targetQ = new TDoubleArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.targetQ.fill(0, variantArraySize, targetQ);
        this.targetV.fill(0, variantArraySize, targetV);
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.voltageRegulation = voltageRegulation;
        if (voltageRegulation != null) {
            voltageRegulation.updateValidable(this);
        }
    }

    @Override
    protected String getTypeDescription() {
        return "AC/DC Voltage Source Converter";
    }

    @Override
    public VoltageSourceConverter setPccTerminal(Terminal pccTerminal) {
        super.setPccTerminal(pccTerminal);
        getOptionalVoltageRegulation().ifPresent(regulation -> regulation.setTerminal(pccTerminal));
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return isWithMode(RegulationMode.VOLTAGE);
    }

    @Override
    public VoltageSourceConverterImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            boolean oldValue = regulation.setRegulating(voltageRegulatorOn);
            String variantId = n.getVariantManager().getVariantId(variantIndex);
            n.invalidateValidationLevel();
            notifyUpdate(VOLTAGE_REGULATOR_ON_ATTRIBUTE, variantId, oldValue, voltageRegulatorOn);
        });
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return this.getRegulatingTargetV();
    }

    @Override
    public VoltageSourceConverterImpl setVoltageSetpoint(double voltageSetpoint) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            if (isWithMode(RegulationMode.VOLTAGE)) {
                double oldValue = regulation.getTargetValue();
                regulation.setTargetValue(voltageSetpoint);
                String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
                notifyUpdate(VOLTAGE_SETPOINT_ATTRIBUTE, variantId, oldValue, voltageSetpoint);
            }
        });
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return this.getRegulatingTargetQ();
    }

    @Override
    public VoltageSourceConverterImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        getOptionalVoltageRegulation().ifPresent(regulation -> {
            if (isWithMode(RegulationMode.REACTIVE_POWER)) {
                double oldValue = regulation.getTargetValue();
                regulation.setTargetValue(reactivePowerSetpoint);
                String variantId = getNetwork().getVariantManager().getVariantId(getNetwork().getVariantIndex());
                notifyUpdate(REACTIVE_POWER_SETPOINT_ATTRIBUTE, variantId, oldValue, reactivePowerSetpoint);
            }
        });
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
        targetQ.ensureCapacity(targetQ.size() + number);
        targetQ.fill(initVariantArraySize, initVariantArraySize + number, targetQ.get(sourceIndex));
        targetV.ensureCapacity(targetV.size() + number);
        targetV.fill(initVariantArraySize, initVariantArraySize + number, targetV.get(sourceIndex));
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.extendVariantArraySize(initVariantArraySize, number, sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetQ.remove(targetQ.size() - number, number);
        targetV.remove(targetV.size() - number, number);
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.deleteVariantArrayElement(number));
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetQ.set(index, targetQ.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
        this.getOptionalVoltageRegulation().ifPresent(vr -> vr.allocateVariantArrayElement(indexes, sourceIndex));
    }

    @Override
    public void remove() {
        this.removeVoltageRegulation();
        super.remove();
    }

    @Override
    protected VoltageSourceConverter self() {
        return this;
    }

    @Override
    public VoltageRegulationBuilder newVoltageRegulation() {
        return new VoltageRegulationBuilderImpl<>(VoltageSourceConverter.class, this, getNetwork().getRef(), this::setVoltageRegulation);
    }

    @Override
    public VoltageRegulation newVoltageRegulation(VoltageRegulation voltageRegulation) {
        this.setVoltageRegulation((VoltageRegulationImpl) voltageRegulation);
        return this.voltageRegulation;
    }

    @Override
    public VoltageRegulation getVoltageRegulation() {
        return this.voltageRegulation;
    }

    private Optional<VoltageRegulationImpl> getOptionalVoltageRegulation() {
        return Optional.ofNullable(this.voltageRegulation);
    }

    @Override
    public void removeVoltageRegulation() {
        this.getOptionalVoltageRegulation().ifPresent(VoltageRegulationImpl::removeTerminal);
        this.voltageRegulation = null;
    }

    @Override
    public Terminal getTerminal() {
        return getPccTerminal();
    }

    private void setVoltageRegulation(VoltageRegulationImpl voltageRegulation) {
        this.removeVoltageRegulation();
        this.voltageRegulation = voltageRegulation;
    }

    @Override
    public double getTargetV() {
        return this.targetV.get(getNetwork().getVariantIndex());
    }

    @Override
    public double getTargetQ() {
        return this.targetQ.get(getNetwork().getVariantIndex());
    }

    @Override
    public VoltageSourceConverter setTargetQ(double targetQ) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetQ, "targetQ");
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetQ.set(variantIndex, targetQ);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetQ", variantId, oldValue, targetQ);
        return this;
    }

    @Override
    public VoltageSourceConverter setTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkDoublePositive(this, targetV, "targetV");
        int variantIndex = n.getVariantIndex();
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("targetV", variantId, oldValue, targetV);
        return this;
    }
}
