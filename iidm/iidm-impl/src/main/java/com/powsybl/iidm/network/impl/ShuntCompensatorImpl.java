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

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private final Ref<? extends VariantManagerHolder> network;

    private final ShuntCompensatorModelWrapper model;

    /* the regulating terminal */
    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    /* the current number of section switched on */
    private final TIntArrayList currentSectionCount;

    /* the regulating status */
    private final TBooleanArrayList voltageRegulatorOn;

    /* the target voltage value */
    private final TDoubleArrayList targetV;

    /* the target deadband */
    private final TDoubleArrayList targetDeadband;

    ShuntCompensatorImpl(Ref<? extends VariantManagerHolder> network,
                         String id, String name, boolean fictitious, ShuntCompensatorModelWrapper model,
                         int currentSectionCount, TerminalExt regulatingTerminal, boolean voltageRegulatorOn,
                         double targetV, double targetDeadband) {
        super(id, name, fictitious);
        this.network = network;
        this.model = attach(model);
        this.regulatingTerminal = regulatingTerminal;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.currentSectionCount = new TIntArrayList(variantArraySize);
        this.voltageRegulatorOn = new TBooleanArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.currentSectionCount.add(currentSectionCount);
            this.voltageRegulatorOn.add(voltageRegulatorOn);
            this.targetV.add(targetV);
            this.targetDeadband.add(targetDeadband);
        }
    }

    private ShuntCompensatorModelWrapper attach(ShuntCompensatorModelWrapper model) {
        model.setShuntCompensator(this);
        return model;
    }

    @Override
    public ConnectableType getType() {
        return ConnectableType.SHUNT_COMPENSATOR;
    }

    @Override
    public TerminalExt getTerminal() {
        return terminals.get(0);
    }

    @Override
    public int getCurrentSectionCount() {
        return currentSectionCount.get(network.get().getVariantIndex());
    }

    @Override
    public int getMaximumSectionCount() {
        return model.getMaximumSectionCount();
    }

    @Override
    public ShuntCompensatorImpl setCurrentSectionCount(int currentSectionCount) {
        ValidationUtil.checkSections(this, currentSectionCount, model.getMaximumSectionCount());
        if (!model.containsSection(currentSectionCount)) {
            throw new ValidationException(this, "unexpected section number (" + currentSectionCount + "): no existing associated section");
        }
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.currentSectionCount.set(variantIndex, currentSectionCount);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("currentSectionCount", variantId, oldValue, currentSectionCount);
        return this;
    }

    @Override
    public double getCurrentB() {
        return model.getB(currentSectionCount.get(network.get().getVariantIndex()));
    }

    @Override
    public double getCurrentG() {
        return model.getG(currentSectionCount.get(network.get().getVariantIndex()));
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
        ValidationUtil.checkRegulatingTerminal(this, regulatingTerminal, getNetwork());
        Terminal oldValue = this.regulatingTerminal;
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        notifyUpdate("regulatingTerminal", oldValue, this.regulatingTerminal);
        return this;
    }

    @Override
    public boolean isVoltageRegulatorOn() {
        return voltageRegulatorOn.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setVoltageRegulatorOn(boolean voltageRegulatorOn) {
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn, targetV.get(variantIndex));
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", voltageRegulatorOn, targetDeadband.get(variantIndex));
        boolean oldValue = this.voltageRegulatorOn.set(variantIndex, voltageRegulatorOn);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("voltageRegulatorOn", variantId, oldValue, voltageRegulatorOn);
        return this;
    }

    @Override
    public double getTargetV() {
        return targetV.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetV(double targetV) {
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkVoltageControl(this, voltageRegulatorOn.get(variantIndex), targetV);
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public double getTargetDeadband() {
        return targetDeadband.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setTargetDeadband(double targetDeadband) {
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(this, "shunt compensator", this.voltageRegulatorOn.get(variantIndex), targetDeadband);
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetDeadband", variantId, oldValue, targetDeadband);
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        currentSectionCount.ensureCapacity(currentSectionCount.size() + number);
        voltageRegulatorOn.ensureCapacity(voltageRegulatorOn.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        for (int i = 0; i < number; i++) {
            currentSectionCount.add(currentSectionCount.get(sourceIndex));
            voltageRegulatorOn.add(voltageRegulatorOn.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        currentSectionCount.remove(currentSectionCount.size() - number, number);
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
            currentSectionCount.set(index, currentSectionCount.get(sourceIndex));
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
