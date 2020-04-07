/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class StaticVarCompensatorImpl extends AbstractConnectable<StaticVarCompensator> implements StaticVarCompensator {

    static final String TYPE_DESCRIPTION = "staticVarCompensator";

    private double bMin;

    private double bMax;

    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    private final TDoubleArrayList voltageSetPoint;

    private final TDoubleArrayList reactivePowerSetPoint;

    private final TIntArrayList regulationMode;

    StaticVarCompensatorImpl(String id, String name, boolean fictitious, double bMin, double bMax, double voltageSetPoint, double reactivePowerSetPoint,
                             RegulationMode regulationMode, TerminalExt regulatingTerminal, Ref<? extends VariantManagerHolder> ref) {
        super(id, name, fictitious);
        this.bMin = bMin;
        this.bMax = bMax;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.voltageSetPoint = new TDoubleArrayList(variantArraySize);
        this.reactivePowerSetPoint = new TDoubleArrayList(variantArraySize);
        this.regulationMode = new TIntArrayList(variantArraySize);
        this.regulatingTerminal = regulatingTerminal;
        for (int i = 0; i < variantArraySize; i++) {
            this.voltageSetPoint.add(voltageSetPoint);
            this.reactivePowerSetPoint.add(reactivePowerSetPoint);
            this.regulationMode.add(regulationMode.ordinal());
        }
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.STATIC_VAR_COMPENSATOR;
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
    public double getVoltageSetPoint() {
        return voltageSetPoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetPoint(double voltageSetPoint) {
        ValidationUtil.checkSvcRegulator(this, voltageSetPoint, getReactivePowerSetPoint(), getRegulationMode());
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.voltageSetPoint.set(variantIndex, voltageSetPoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageSetpoint", variantId, oldValue, voltageSetPoint);
        return this;
    }

    @Override
    public double getReactivePowerSetPoint() {
        return reactivePowerSetPoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetPoint(double reactivePowerSetPoint) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetPoint(), reactivePowerSetPoint, getRegulationMode());
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.reactivePowerSetPoint.set(variantIndex, reactivePowerSetPoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("reactivePowerSetpoint", variantId, oldValue, reactivePowerSetPoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.values()[regulationMode.get(getNetwork().getVariantIndex())];
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkSvcRegulator(this, getVoltageSetPoint(), getReactivePowerSetPoint(), regulationMode);
        int variantIndex = getNetwork().getVariantIndex();
        RegulationMode oldValue = RegulationMode.values()[this.regulationMode.set(variantIndex,
                regulationMode.ordinal())];
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("regulationMode", variantId, oldValue, regulationMode);
        return this;
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public StaticVarCompensatorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        voltageSetPoint.ensureCapacity(voltageSetPoint.size() + number);
        reactivePowerSetPoint.ensureCapacity(reactivePowerSetPoint.size() + number);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        for (int i = 0; i < number; i++) {
            voltageSetPoint.add(voltageSetPoint.get(sourceIndex));
            reactivePowerSetPoint.add(reactivePowerSetPoint.get(sourceIndex));
            regulationMode.add(regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        voltageSetPoint.remove(voltageSetPoint.size() - number, number);
        reactivePowerSetPoint.remove(reactivePowerSetPoint.size() - number, number);
        regulationMode.remove(regulationMode.size() - number, number);
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
            voltageSetPoint.set(index, voltageSetPoint.get(sourceIndex));
            reactivePowerSetPoint.set(index, reactivePowerSetPoint.get(sourceIndex));
            regulationMode.set(index, regulationMode.get(sourceIndex));
        }
    }

}
