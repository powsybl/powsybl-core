/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.ReactiveLimits;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class VscConverterStationImpl extends AbstractHvdcConverterStation<VscConverterStation> implements VscConverterStation, ReactiveLimitsOwner {

    static final String TYPE_DESCRIPTION = "vscConverterStation";

    static final String VALIDABLE_TYPE_DESCRIPTION = "vsc converter station";

    private final ReactiveLimitsHolderImpl reactiveLimits;

    private final TBooleanArrayList voltageRegulatorOn;

    private final TDoubleArrayList reactivePowerSetpoint;

    private final TDoubleArrayList voltageSetpoint;

    private TerminalExt regulatingTerminal;

    VscConverterStationImpl(String id, String name, boolean fictitious, float lossFactor, Ref<NetworkImpl> ref,
                            boolean voltageRegulatorOn, double reactivePowerSetpoint, double voltageSetpoint, TerminalExt regulatingTerminal) {
        super(ref, id, name, fictitious, lossFactor);
        int variantArraySize = ref.get().getVariantManager().getVariantArraySize();
        this.voltageRegulatorOn = new TBooleanArrayList(variantArraySize);
        this.reactivePowerSetpoint = new TDoubleArrayList(variantArraySize);
        this.voltageSetpoint = new TDoubleArrayList(variantArraySize);
        this.voltageRegulatorOn.fill(0, variantArraySize, voltageRegulatorOn);
        this.reactivePowerSetpoint.fill(0, variantArraySize, reactivePowerSetpoint);
        this.voltageSetpoint.fill(0, variantArraySize, voltageSetpoint);
        this.reactiveLimits = new ReactiveLimitsHolderImpl(this, new MinMaxReactiveLimitsImpl(-Double.MAX_VALUE, Double.MAX_VALUE));
        this.regulatingTerminal = regulatingTerminal;
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
        return voltageRegulatorOn.get(getNetwork().getVariantIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkVoltageControlAndTargetQ(this, regulatingTerminal,
            voltageSetpoint.get(variantIndex), reactivePowerSetpoint.get(variantIndex), voltageRegulatorOn,
            getNetwork());
        boolean oldValue = this.voltageRegulatorOn.get(variantIndex);
        this.voltageRegulatorOn.set(variantIndex, voltageRegulatorOn);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public double getVoltageSetpoint() {
        return this.voltageSetpoint.get(getNetwork().getVariantIndex());
    }

    @Override
    public VscConverterStationImpl setVoltageSetpoint(double voltageSetpoint) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkVoltageSetpoint(this, VALIDABLE_TYPE_DESCRIPTION, voltageSetpoint, voltageRegulatorOn.get(variantIndex));
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
    public VscConverterStationImpl setReactivePowerSetpoint(double reactivePowerSetpoint) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkReactivePowerTarget(this, VALIDABLE_TYPE_DESCRIPTION, reactivePowerSetpoint, voltageRegulatorOn.get(variantIndex));
        double oldValue = this.reactivePowerSetpoint.set(variantIndex, reactivePowerSetpoint);
        String variantId = getNetwork().getVariantManager().getVariantId(variantIndex);
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
    public <RL extends ReactiveLimits> RL getReactiveLimits(Class<RL> type) {
        return reactiveLimits.getReactiveLimits(type);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);

        reactivePowerSetpoint.ensureCapacity(reactivePowerSetpoint.size() + number);
        reactivePowerSetpoint.fill(initVariantArraySize, initVariantArraySize + number, reactivePowerSetpoint.get(sourceIndex));

        voltageSetpoint.ensureCapacity(voltageSetpoint.size() + number);
        voltageSetpoint.fill(initVariantArraySize, initVariantArraySize + number, voltageSetpoint.get(sourceIndex));

        voltageRegulatorOn.ensureCapacity(voltageRegulatorOn.size() + number);
        voltageRegulatorOn.fill(initVariantArraySize, initVariantArraySize + number, voltageRegulatorOn.get(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        reactivePowerSetpoint.remove(reactivePowerSetpoint.size() - number, number);
        voltageSetpoint.remove(voltageSetpoint.size() - number, number);
        voltageRegulatorOn.remove(voltageRegulatorOn.size() - number, number);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            voltageRegulatorOn.set(index, voltageRegulatorOn.get(sourceIndex));
            reactivePowerSetpoint.set(index, reactivePowerSetpoint.get(sourceIndex));
            voltageSetpoint.set(index, voltageSetpoint.get(sourceIndex));
        }
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public VscConverterStationImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        int variantIndex = getNetwork().getVariantIndex();
        ValidationUtil.checkRegulatingTerminal(this, VALIDABLE_TYPE_DESCRIPTION, regulatingTerminal, voltageRegulatorOn.get(variantIndex), getNetwork());
        Terminal oldValue = this.regulatingTerminal;
        this.regulatingTerminal = (TerminalExt) regulatingTerminal;
        notifyUpdate("regulatingTerminal", oldValue, regulatingTerminal);
        return this;
    }
}
