/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcVoltageSourceConverter;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.ValidationUtil;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcVoltageSourceConverterImpl extends AbstractDcConverter<DcVoltageSourceConverter> implements DcVoltageSourceConverter, ReactiveLimitsOwner {

    public static final String VOLTAGE_REGULATOR_ON_ATTRIBUTE = "voltageRegulatorOn";
    public static final String VOLTAGE_SETPOINT_ATTRIBUTE = "voltageSetpoint";
    public static final String REACTIVE_POWER_SETPOINT_ATTRIBUTE = "reactivePowerSetpoint";

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private final TDoubleArrayList reactivePowerSetpoint;

    private final TDoubleArrayList voltageSetpoint;

    private final RegulatingPoint regulatingPoint;

    DcVoltageSourceConverterImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                                 double idleLoss, double switchingLoss, double resistiveLoss,
                                 TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc,
                                 boolean voltageRegulatorOn, double reactivePowerSetpoint, double voltageSetpoint) {
        super(ref, id, name, fictitious, idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.reactivePowerSetpoint = new TDoubleArrayList(variantArraySize);
        this.voltageSetpoint = new TDoubleArrayList(variantArraySize);
        this.reactivePowerSetpoint.fill(0, variantArraySize, reactivePowerSetpoint);
        this.voltageSetpoint.fill(0, variantArraySize, voltageSetpoint);
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        regulatingPoint = new RegulatingPoint(id, () -> null, variantArraySize, voltageRegulatorOn, true);
        regulatingPoint.setRegulatingTerminal(pccTerminal);
    }

    @Override
    protected String getTypeDescription() {
        return "DC Voltage Source Converter";
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, VOLTAGE_REGULATOR_ON_ATTRIBUTE);
        return regulatingPoint.isRegulating(getNetwork().getVariantIndex());
    }

    @Override
    public DcVoltageSourceConverterImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, VOLTAGE_REGULATOR_ON_ATTRIBUTE);
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, voltageSetpoint.get(variantIndex), reactivePowerSetpoint.get(variantIndex),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        boolean oldValue = this.regulatingPoint.isRegulating(variantIndex);
        this.regulatingPoint.setRegulating(variantIndex, voltageRegulatorOn);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(VOLTAGE_REGULATOR_ON_ATTRIBUTE, variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, VOLTAGE_SETPOINT_ATTRIBUTE);
        return this.voltageSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public DcVoltageSourceConverterImpl setVoltageSetpoint(double voltageSetpoint) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, VOLTAGE_SETPOINT_ATTRIBUTE);
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        ValidationUtil.checkVoltageControl(this, regulatingPoint.isRegulating(variantIndex), voltageSetpoint, reactivePowerSetpoint.get(variantIndex),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.voltageSetpoint.set(variantIndex, voltageSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(VOLTAGE_SETPOINT_ATTRIBUTE, variantId, oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, REACTIVE_POWER_SETPOINT_ATTRIBUTE);
        return reactivePowerSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public DcVoltageSourceConverterImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, REACTIVE_POWER_SETPOINT_ATTRIBUTE);
        NetworkImpl n = getNetwork();
        int variantIndex = n.getVariantIndex();
        ValidationUtil.checkVoltageControl(this, regulatingPoint.isRegulating(variantIndex), voltageSetpoint.get(variantIndex), reactivePowerSetpoint,
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.reactivePowerSetpoint.set(variantIndex, reactivePowerSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(REACTIVE_POWER_SETPOINT_ATTRIBUTE, variantId, oldValue, reactivePowerSetpoint);
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

        reactivePowerSetpoint.ensureCapacity(reactivePowerSetpoint.size() + number);
        reactivePowerSetpoint.fill(initVariantArraySize, initVariantArraySize + number, reactivePowerSetpoint.get(sourceIndex));

        voltageSetpoint.ensureCapacity(voltageSetpoint.size() + number);
        voltageSetpoint.fill(initVariantArraySize, initVariantArraySize + number, voltageSetpoint.get(sourceIndex));
        regulatingPoint.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        reactivePowerSetpoint.remove(reactivePowerSetpoint.size() - number, number);
        voltageSetpoint.remove(voltageSetpoint.size() - number, number);
        regulatingPoint.reduceVariantArraySize(number);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.get(sourceIndex));
            voltageSetpoint.set(index, voltageSetpoint.get(sourceIndex));
        }
        regulatingPoint.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    public void remove() {
        regulatingPoint.remove();
        super.remove();
    }

    @Override
    protected DcVoltageSourceConverter self() {
        return this;
    }
}
