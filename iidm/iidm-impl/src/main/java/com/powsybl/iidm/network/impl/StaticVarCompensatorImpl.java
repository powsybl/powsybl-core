/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class StaticVarCompensatorImpl extends AbstractConnectable<StaticVarCompensator> implements StaticVarCompensator {

    static final String TYPE_DESCRIPTION = "Static var compensator";

    private double bMin;

    private double bMax;

    private final RegulatingPoint regulatingPoint;

    // attributes depending on the variant

    private final ExtendedDoubleArrayList voltageSetpoint;

    private final ExtendedDoubleArrayList reactivePowerSetpoint;

    StaticVarCompensatorImpl(String id, String name, boolean fictitious, double bMin, double bMax, double voltageSetpoint, double reactivePowerSetpoint,
                             RegulationMode regulationMode, boolean regulating, TerminalExt regulatingTerminal, Ref<NetworkImpl> ref) {
        super(ref, id, name, fictitious);
        this.bMin = bMin;
        this.bMax = bMax;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.voltageSetpoint = new ExtendedDoubleArrayList(variantArraySize, voltageSetpoint);
        this.reactivePowerSetpoint = new ExtendedDoubleArrayList(variantArraySize, reactivePowerSetpoint);
        regulatingPoint = new RegulatingPoint(id, this::getTerminal, variantArraySize, regulationMode != null ? regulationMode.ordinal() : -1, regulating, RegulationMode.VOLTAGE.ordinal(), regulationMode == RegulationMode.VOLTAGE);
        regulatingPoint.setRegulatingTerminal(regulatingTerminal);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    protected String getTypeDescription() {
        return TYPE_DESCRIPTION;
    }

    @Override
    public double getBmin() {
        return bMin;
    }

    @Override
    public StaticVarCompensatorImpl setBmin(double bMin) {
        ValidationUtil.checkBmin(this, bMin);
        double oldValue = this.bMin;
        this.bMin = bMin;
        notifyUpdate("bMin", oldValue, bMin);
        return this;
    }

    @Override
    public double getBmax() {
        return bMax;
    }

    @Override
    public StaticVarCompensatorImpl setBmax(double bMax) {
        ValidationUtil.checkBmax(this, bMax);
        double oldValue = this.bMax;
        this.bMax = bMax;
        notifyUpdate("bMax", oldValue, bMax);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return voltageSetpoint.getDouble(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetpoint(double voltageSetpoint) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkSvcRegulator(this, isRegulating(), voltageSetpoint, getReactivePowerSetpoint(), getRegulationMode(),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        double oldValue = this.voltageSetpoint.set(variantIndex, voltageSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("voltageSetpoint", variantId, oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return reactivePowerSetpoint.getDouble(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkSvcRegulator(this, isRegulating(), getVoltageSetpoint(), reactivePowerSetpoint, getRegulationMode(),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        double oldValue = this.reactivePowerSetpoint.set(variantIndex, reactivePowerSetpoint);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        int variantIndex = getNetwork().getVariantIndex();
        return regulatingPoint.getRegulationMode(variantIndex) != -1 ? RegulationMode.values()[regulatingPoint.getRegulationMode(variantIndex)] : null;
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkSvcRegulator(this, isRegulating(), getVoltageSetpoint(), getReactivePowerSetpoint(), regulationMode,
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = n.getVariantIndex();
        int oldValueOrdinal = regulatingPoint.setRegulationMode(variantIndex,
                regulationMode != null ? regulationMode.ordinal() : -1);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate("regulationMode", variantId, oldValueOrdinal == -1 ? null : RegulationMode.values()[oldValueOrdinal], regulationMode);
        return this;
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingPoint.getRegulatingTerminal();
    }

    @Override
    public StaticVarCompensatorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        Terminal oldValue = regulatingPoint.getRegulatingTerminal();
        regulatingPoint.setRegulatingTerminal((TerminalExt) regulatingTerminal);
        notifyUpdate("regulatingTerminal", oldValue, regulatingPoint.getRegulatingTerminal());
        return this;
    }

    @Override
    public void remove() {
        regulatingPoint.remove();
        super.remove();
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        voltageSetpoint.growAndFill(number, voltageSetpoint.getDouble(sourceIndex));
        reactivePowerSetpoint.growAndFill(number, reactivePowerSetpoint.getDouble(sourceIndex));
        regulatingPoint.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        voltageSetpoint.removeElements(number);
        reactivePowerSetpoint.removeElements(number);
        regulatingPoint.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        regulatingPoint.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            voltageSetpoint.set(index, voltageSetpoint.getDouble(sourceIndex));
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.getDouble(sourceIndex));
        }
        regulatingPoint.allocateVariantArrayElement(indexes, sourceIndex);
    }

    @Override
    public boolean isRegulating() {
        int variantIndex = getNetwork().getVariantIndex();
        return regulatingPoint.isRegulating(variantIndex);
    }

    @Override
    public StaticVarCompensator setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkSvcRegulator(this, regulating, getVoltageSetpoint(), getReactivePowerSetpoint(), getRegulationMode(),
                n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        int variantIndex = getNetwork().getVariantIndex();
        this.regulatingPoint.setRegulating(variantIndex, regulating);
        return this;
    }

}
