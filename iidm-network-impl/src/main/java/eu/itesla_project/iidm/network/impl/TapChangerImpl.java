/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.Terminal;
import eu.itesla_project.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TIntArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class TapChangerImpl<H extends TapChangerParent, C extends TapChangerImpl<H, C, S>, S extends TapChangerStepImpl<S>> implements Stateful {

    protected final Ref<? extends MultiStateObject> network;

    protected final H parent;

    protected int lowStepPosition;

    protected final List<S> steps;

    private final TerminalExt terminal;

    // attributes depending on the state

    protected final TIntArrayList currentStepPosition;

    protected final BitSet regulating;

    protected TapChangerImpl(Ref<? extends MultiStateObject> network, H parent,
                             int lowStepPosition, List<S> steps, TerminalExt terminal,
                             int currentStepPosition, boolean regulating) {
        this.network = network;
        this.parent = parent;
        this.lowStepPosition = lowStepPosition;
        this.steps = steps;
        this.terminal = terminal;
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.currentStepPosition = new TIntArrayList(stateArraySize);
        this.regulating = new BitSet(stateArraySize);
        this.regulating.set(0, stateArraySize, regulating);
        for (int i = 0; i < stateArraySize; i++) {
            this.currentStepPosition.add(currentStepPosition);
        }
    }

    protected abstract NetworkImpl getNetwork();

    public int getStepCount() {
        return steps.size();
    }

    public int getLowStepPosition() {
        return lowStepPosition;
    }

    public int getHighStepPosition() {
        return lowStepPosition + steps.size() - 1;
    }

    public int getCurrentStepPosition() {
        return currentStepPosition.get(network.get().getStateIndex());
    }

    protected abstract String getTapChangerAttribute();

    public C setCurrentStepPosition(int currentStepPosition) {
        if (currentStepPosition < lowStepPosition
                || currentStepPosition > getHighStepPosition()) {
            throw new ValidationException(parent, "incorrect current step position "
                    + currentStepPosition + " [" + lowStepPosition + ", "
                    + getHighStepPosition() + "]");
        }
        int oldValue = this.currentStepPosition.set(network.get().getStateIndex(), currentStepPosition);
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), getTapChangerAttribute() + ".tapPosition", oldValue, currentStepPosition);
        return (C) this;
    }

    public S getStep(int position) {
        if (position < lowStepPosition || position > getHighStepPosition()) {
            throw new ValidationException(parent, "incorrect step position "
                    + position + " [" + lowStepPosition + ", " + getHighStepPosition()
                    + "]");
        }
        return steps.get(position - lowStepPosition);
    }

    public S getCurrentStep() {
        return getStep(getCurrentStepPosition());
    }

    public boolean isRegulating() {
        return regulating.get(network.get().getStateIndex());
    }

    public C setRegulating(boolean regulating) {
        if (regulating && terminal == null) {
            throw new ValidationException(parent,
                    "cannot change the regulating status if the regulation terminal is not set");
        }
        this.regulating.set(network.get().getStateIndex(), regulating);
        return (C) this;
    }

    public TerminalExt getTerminal() {
        return terminal;
    }

    public void setTerminal(Terminal t) {
        if (terminal == null) {
            throw new ValidationException(parent, "regulation terminal is null");
        }
        if (terminal.getVoltageLevel().getNetwork() != getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        currentStepPosition.ensureCapacity(currentStepPosition.size() + number);
        for (int i = 0; i < number; i++) {
            regulating.set(initStateArraySize + i, regulating.get(sourceIndex));
            currentStepPosition.add(currentStepPosition.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        currentStepPosition.remove(currentStepPosition.size() - number, number);
    }

    @Override
    public void deleteStateArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateStateArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            regulating.set(index, regulating.get(sourceIndex));
            currentStepPosition.set(index, currentStepPosition.get(sourceIndex));
        }
    }

}
