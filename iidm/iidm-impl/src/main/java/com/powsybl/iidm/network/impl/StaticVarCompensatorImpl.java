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
import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.validation.Validation;
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

    private final TDoubleArrayList voltageSetpoint;

    private final TDoubleArrayList reactivePowerSetpoint;

    private final TIntArrayList regulationMode;

    StaticVarCompensatorImpl(String id, String name, boolean fictitious, double bMin, double bMax, double voltageSetpoint, double reactivePowerSetpoint,
                             RegulationMode regulationMode, TerminalExt regulatingTerminal, Ref<NetworkImpl> ref) {
        super(ref, id, name, fictitious);
        this.bMin = bMin;
        this.bMax = bMax;
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.voltageSetpoint = new TDoubleArrayList(variantArraySize);
        this.reactivePowerSetpoint = new TDoubleArrayList(variantArraySize);
        this.regulationMode = new TIntArrayList(variantArraySize);
        this.regulatingTerminal = regulatingTerminal;
        for (int i = 0; i < variantArraySize; i++) {
            this.voltageSetpoint.add(voltageSetpoint);
            this.reactivePowerSetpoint.add(reactivePowerSetpoint);
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
        Validation.getDefault().checkBmin(this, bMin);
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
        Validation.getDefault().checkBmax(this, bMax);
        double oldValue = this.bMax;
        this.bMax = bMax;
        notifyUpdate("bMax", oldValue, bMax);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return voltageSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setVoltageSetpoint(double voltageSetpoint) {
        Validation.getDefault().checkSvcRegulator(this, voltageSetpoint, getReactivePowerSetpoint(), getRegulationMode());
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.voltageSetpoint.set(variantIndex, voltageSetpoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageSetpoint", variantId, oldValue, voltageSetpoint);
        return this;
    }

    @Override
    public double getReactivePowerSetpoint() {
        return reactivePowerSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public StaticVarCompensatorImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        Validation.getDefault().checkSvcRegulator(this, getVoltageSetpoint(), reactivePowerSetpoint, getRegulationMode());
        int variantIndex = getNetwork().getVariantIndex();
        double oldValue = this.reactivePowerSetpoint.set(variantIndex, reactivePowerSetpoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("reactivePowerSetpoint", variantId, oldValue, reactivePowerSetpoint);
        return this;
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.values()[regulationMode.get(getNetwork().getVariantIndex())];
    }

    @Override
    public StaticVarCompensatorImpl setRegulationMode(RegulationMode regulationMode) {
        Validation.getDefault().checkSvcRegulator(this, getVoltageSetpoint(), getReactivePowerSetpoint(), regulationMode);
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
        Validation.getDefault().checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        voltageSetpoint.ensureCapacity(voltageSetpoint.size() + number);
        reactivePowerSetpoint.ensureCapacity(reactivePowerSetpoint.size() + number);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        for (int i = 0; i < number; i++) {
            voltageSetpoint.add(voltageSetpoint.get(sourceIndex));
            reactivePowerSetpoint.add(reactivePowerSetpoint.get(sourceIndex));
            regulationMode.add(regulationMode.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        voltageSetpoint.remove(voltageSetpoint.size() - number, number);
        reactivePowerSetpoint.remove(reactivePowerSetpoint.size() - number, number);
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
            voltageSetpoint.set(index, voltageSetpoint.get(sourceIndex));
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.get(sourceIndex));
            regulationMode.set(index, regulationMode.get(sourceIndex));
        }
    }

}
