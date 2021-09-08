/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private final NetworkImpl network;

    private final ShuntCompensatorModelExt model;

    /* the regulating terminal */
    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    /* the current number of section switched on */
    private final TIntArrayList sectionCount;

    /* the regulating status */
    private final TBooleanArrayList voltageRegulatorOn;

    /* the target voltage value */
    private final TDoubleArrayList targetV;

    /* the target deadband */
    private final TDoubleArrayList targetDeadband;

    ShuntCompensatorImpl(Ref<NetworkImpl> networkRef,
                         String id, String name, boolean fictitious, ShuntCompensatorModelExt model,
                         int sectionCount, TerminalExt regulatingTerminal, boolean voltageRegulatorOn,
                         double targetV, double targetDeadband) {
        super(networkRef, id, name, fictitious);
        network = networkRef.get();
        this.regulatingTerminal = regulatingTerminal;
        int variantArraySize = network.getVariantManager().getVariantArraySize();
        this.sectionCount = new TIntArrayList(variantArraySize);
        this.voltageRegulatorOn = new TBooleanArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.sectionCount.add(sectionCount);
            this.voltageRegulatorOn.add(voltageRegulatorOn);
            this.targetV.add(targetV);
            this.targetDeadband.add(targetDeadband);
        }
        this.model = Objects.requireNonNull(model).attach(this);
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public int getSectionCount() {
        return sectionCount.get(network.getVariantIndex());
    }

    @Override
    public int getMaximumSectionCount() {
        return model.getMaximumSectionCount();
    }

    @Override
    public ShuntCompensatorImpl setSectionCount(int sectionCount) {
        ValidationUtil.checkSections(this, sectionCount, model.getMaximumSectionCount(), network.areValidationChecksEnabled());
        if (sectionCount < 0 || sectionCount > model.getMaximumSectionCount()) {
            throw new ValidationException(this, "unexpected section number (" + sectionCount + "): no existing associated section");
        }
        int variantIndex = network.getVariantIndex();
        int oldValue = this.sectionCount.set(variantIndex, sectionCount);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("sectionCount", variantId, oldValue, sectionCount);
        network.uncheckValidationStatusIfDisabledCheck();
        return this;
    }

    @Override
    public double getB() {
        return model.getB(sectionCount.get(network.getVariantIndex()));
    }

    @Override
    public double getG() {
        return model.getG(sectionCount.get(network.getVariantIndex()));
    }

    @Override
    public double getB(int sectionCount) {
        return model.getB(sectionCount);
    }

    @Override
    public double getG(int sectionCount) {
        return model.getG(sectionCount);
    }

    @Override
    public ShuntCompensatorModelType getModelType() {
        return model.getType();
    }

    @Override
    public ShuntCompensatorModel getModel() {
        return model;
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> modelType) {
        if (modelType == null) {
            throw new IllegalArgumentException("shunt compensator model type is null");
        }
        if (modelType.isInstance(model)) {
            return modelType.cast(model);
        }
        throw new ValidationException(this, "incorrect shunt compensator model type " +
                modelType.getName() + ", expected " + model.getClass());
    }

    @Override
    public TerminalExt getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public ShuntCompensatorImpl setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, network);
        Terminal oldValue = this.regulatingTerminal;
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        notifyUpdate("regulatingTerminal", oldValue, this.regulatingTerminal);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(network.getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int variantIndex = network.getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(variantIndex), network.areValidationChecksEnabled());
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband.get(variantIndex), network.areValidationChecksEnabled());
        boolean oldValue = this.voltageRegulatorOn.set(variantIndex, voltageRegulatorOn);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        network.uncheckValidationStatusIfDisabledCheck();
        return this;
    }

    @Override
    public double getTargetV() {
        return targetV.get(network.getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetV(double targetV) {
        int variantIndex = network.getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(variantIndex), targetV, network.areValidationChecksEnabled());
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetV", variantId, oldValue, targetV);
        network.uncheckValidationStatusIfDisabledCheck();
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(network.getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetDeadband(double targetDeadband) {
        int variantIndex = network.getVariantIndex();
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", this.voltageRegulatorOn.get(variantIndex), targetDeadband, network.areValidationChecksEnabled());
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetDeadband", variantId, oldValue, targetDeadband);
        network.uncheckValidationStatusIfDisabledCheck();
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        sectionCount.ensureCapacity(sectionCount.size() + number);
        voltageRegulatorOn.ensureCapacity(voltageRegulatorOn.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        for (int i = 0; i < number; i++) {
            sectionCount.add(sectionCount.get(sourceIndex));
            voltageRegulatorOn.add(voltageRegulatorOn.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        sectionCount.remove(sectionCount.size() - number, number);
        voltageRegulatorOn.remove(voltageRegulatorOn.size() - number, number);
        targetV.remove(targetV.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            sectionCount.set(index, sectionCount.get(sourceIndex));
            voltageRegulatorOn.set(index, voltageRegulatorOn.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

}
