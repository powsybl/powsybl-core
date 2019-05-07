/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.api.impl;

import com.powsybl.iidm.api.ConnectableType;
import com.powsybl.iidm.api.ShuntCompensator;
import com.powsybl.iidm.api.impl.util.Ref;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private final Ref<? extends VariantManagerHolder> network;

    /* susceptance per section */
    private double bPerSection;

    /* the maximum number of section */
    private int maximumSectionCount;

    // attributes depending on the variant

    /* the current number of section switched on */
    private final TIntArrayList currentSectionCount;

    ShuntCompensatorImpl(Ref<? extends VariantManagerHolder> network,
                         String id, String name, double bPerSection, int maximumSectionCount,
                         int currentSectionCount) {
        super(id, name);
        this.network = network;
        this.bPerSection = bPerSection;
        this.maximumSectionCount = maximumSectionCount;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.currentSectionCount = new TIntArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.currentSectionCount.add(currentSectionCount);
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
    public double getbPerSection() {
        return bPerSection;
    }

    @Override
    public ShuntCompensatorImpl setbPerSection(double bPerSection) {
        ValidationUtil.checkbPerSection(this, bPerSection);
        double oldValue = this.bPerSection;
        this.bPerSection = bPerSection;
        notifyUpdate("bPerSection", oldValue, bPerSection);
        return this;
    }

    @Override
    public int getMaximumSectionCount() {
        return maximumSectionCount;
    }

    @Override
    public ShuntCompensatorImpl setMaximumSectionCount(int maximumSectionCount) {
        ValidationUtil.checkSections(this, getCurrentSectionCount(), maximumSectionCount);
        int oldValue = this.maximumSectionCount;
        this.maximumSectionCount = maximumSectionCount;
        notifyUpdate("maximumSectionCount", oldValue, maximumSectionCount);
        return this;
    }

    @Override
    public int getCurrentSectionCount() {
        return currentSectionCount.get(network.get().getVariantIndex());
    }

    @Override
    public ShuntCompensatorImpl setCurrentSectionCount(int currentSectionCount) {
        ValidationUtil.checkSections(this, currentSectionCount, maximumSectionCount);
        int oldValue = this.currentSectionCount.set(network.get().getVariantIndex(), currentSectionCount);
        notifyUpdate("currentSectionCount", oldValue, currentSectionCount);
        return this;
    }

    @Override
    public double getCurrentB() {
        return bPerSection * getCurrentSectionCount();
    }

    @Override
    public double getMaximumB() {
        return bPerSection * maximumSectionCount;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        currentSectionCount.ensureCapacity(currentSectionCount.size() + number);
        for (int i = 0; i < number; i++) {
            currentSectionCount.add(currentSectionCount.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        currentSectionCount.remove(currentSectionCount.size() - number, number);
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
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

}
