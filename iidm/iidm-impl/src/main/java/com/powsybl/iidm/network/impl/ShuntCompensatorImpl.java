/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends AbstractConnectable<ShuntCompensator> implements ShuntCompensator {

    private final Ref<? extends MultiStateObject> network;

    /* susceptance per section */
    private double bPerSection;

    /* the maximum number of section */
    private int maximumSectionCount;

    // attributes depending on the state

    /* the current number of section switched on */
    private final TIntArrayList currentSectionCount;

    ShuntCompensatorImpl(Ref<? extends MultiStateObject> network,
                         String id, String name, double bPerSection, int maximumSectionCount,
                         int currentSectionCount) {
        super(id, name);
        this.network = network;
        this.bPerSection = bPerSection;
        this.maximumSectionCount = maximumSectionCount;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.currentSectionCount = new TIntArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
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
        return currentSectionCount.get(network.get().getStateIndex());
    }

    @Override
    public ShuntCompensatorImpl setCurrentSectionCount(int currentSectionCount) {
        ValidationUtil.checkSections(this, currentSectionCount, maximumSectionCount);
        int oldValue = this.currentSectionCount.set(network.get().getStateIndex(), currentSectionCount);
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
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        currentSectionCount.ensureCapacity(currentSectionCount.size() + number);
        for (int i = 0; i < number; i++) {
            currentSectionCount.add(currentSectionCount.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        currentSectionCount.remove(currentSectionCount.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        super.deleteStateArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateStateArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            currentSectionCount.set(index, currentSectionCount.get(sourceIndex));
        }
    }

    @Override
    protected String getTypeDescription() {
        return "Shunt compensator";
    }

}
