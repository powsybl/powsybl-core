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
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private final Ref<? extends VariantManagerHolder> network;

    private final ShuntCompensatorModelHolder model;

    private TerminalExt regulatingTerminal;

    // attributes depending on the variant

    /* the current number of section switched on */
    private final TIntArrayList currentSectionCount;

    private final TDoubleArrayList targetV;

    private final TDoubleArrayList targetDeadband;

    private final TBooleanArrayList regulating;

    ShuntCompensatorImpl(Ref<? extends VariantManagerHolder> network,
                         String id, String name,
                         ShuntCompensatorModelHolder model,
                         int currentSectionCount, boolean regulating, double targetV, double targetDeadband,
                         TerminalExt regulatingTerminal) {
        super(id, name);
        this.network = network;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.model = model;
        this.regulatingTerminal = regulatingTerminal;
        this.currentSectionCount = new TIntArrayList(variantArraySize);
        this.targetV = new TDoubleArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.currentSectionCount.add(currentSectionCount);
            this.regulating.add(regulating);
            this.targetV.add(targetV);
            this.targetDeadband.add(targetDeadband);
        }
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
    public ShuntCompensatorModelType getModelType() {
        return model.getType();
    }

    @Override
    public <M extends ShuntCompensatorModel> M getModel(Class<M> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(model)) {
            return type.cast(model);
        }
        throw new ValidationException(this, "incorrect shunt compensator model type "
                    + type.getName() + ", expected " + model.getClass());
    }

    @Override
    public int getCurrentSectionCount() {
        return currentSectionCount.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setCurrentSectionCount(int currentSectionCount) {
        model.checkCurrentSection(currentSectionCount);
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
    public double getMaximumB() {
        return model.getMaximumB();
    }

    @Override
    public Terminal getRegulatingTerminal() {
        return regulatingTerminal;
    }

    @Override
    public ShuntCompensator setRegulatingTerminal(Terminal regulatingTerminal) {
        ValidationUtil.checkRegulatingTerminal(this, (TerminalExt) regulatingTerminal, getNetwork());
        this.regulatingTerminal = regulatingTerminal != null ? (TerminalExt) regulatingTerminal : getTerminal();
        return this;
    }

    @Override
    public boolean isRegulating() {
        return regulating.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensator setRegulating(boolean regulating) {
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkShuntCompensatorRegulation(this, regulating, targetV.get(variantIndex), targetDeadband.get(variantIndex));
        boolean oldValue = this.regulating.set(variantIndex, regulating);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("regulating", variantId, oldValue, regulating);
        return this;
    }

    @Override
    public double getTargetV() {
        return targetV.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensator setTargetV(double targetV) {
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkShuntCompensatorRegulation(this, regulating.get(variantIndex), targetV, targetDeadband.get(variantIndex));
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
    public ShuntCompensator setTargetDeadband(double targetDeadband) {
        int variantIndex = network.get().getVariantIndex();
        double newTargetDeadband = Double.isNaN(targetDeadband) ? 0.0 : targetDeadband;
        ValidationUtil.checkShuntCompensatorRegulation(this, regulating.get(variantIndex), targetV.get(variantIndex), newTargetDeadband);
        double oldValue = this.targetDeadband.set(variantIndex, newTargetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        notifyUpdate("targetDeadband", variantId, oldValue, newTargetDeadband);
        return this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        currentSectionCount.ensureCapacity(currentSectionCount.size() + number);
        regulating.ensureCapacity(regulating.size() + number);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; i++) {
            currentSectionCount.add(currentSectionCount.get(sourceIndex));
            regulating.add(regulating.get(sourceIndex));
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        currentSectionCount.remove(currentSectionCount.size() - number, number);
        regulating.remove(regulating.size() - number, number);
        targetV.remove(targetV.size() - number, number);
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
            regulating.set(index, regulating.get(sourceIndex));
            targetV.set(index, targetV.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

}
