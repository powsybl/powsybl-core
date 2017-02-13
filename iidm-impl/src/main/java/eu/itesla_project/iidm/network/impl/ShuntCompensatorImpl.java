/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.ConnectableType;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TIntArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ShuntCompensatorImpl extends ConnectableImpl<ShuntCompensator> implements ShuntCompensator {

    private final Ref<? extends MultiStateObject> network;

    /* susceptance per section */
    private float bPerSection;

    /* the maximum number of section */
    private int maximumSectionCount;

    // attributes depending on the state

    /* the current number of section switched on */
    private final TIntArrayList currentSectionCount;

    ShuntCompensatorImpl(Ref<? extends MultiStateObject> network,
                         String id, String name, float bPerSection, int maximumSectionCount,
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
    public float getbPerSection() {
        return bPerSection;
    }

    @Override
    public ShuntCompensatorImpl setbPerSection(float bPerSection) {
        ValidationUtil.checkbPerSection(this, bPerSection);
        float oldValue = this.bPerSection;
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
        float oldValue = this.maximumSectionCount;
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
    public float getCurrentB() {
        return bPerSection * getCurrentSectionCount();
    }

    @Override
    public float getMaximumB() {
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
