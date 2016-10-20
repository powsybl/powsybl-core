/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.PhaseTapChanger;
import gnu.trove.list.array.TFloatArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.BitSet;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerImpl extends TapChangerImpl<TwoWindingsTransformerImpl, PhaseTapChangerImpl, PhaseTapChangerStepImpl>
                          implements PhaseTapChanger {

    // attributes depending on the state

    private final TIntArrayList regulationMode;

    private final TFloatArrayList regulationValue;

    PhaseTapChangerImpl(TwoWindingsTransformerImpl parent, int lowTapPosition,
                        List<PhaseTapChangerStepImpl> steps, TerminalExt terminal,
                        int tapPosition, RegulationMode regulationMode, float regulationValue) {
        super(parent.getNetwork().getRef(), parent, lowTapPosition, steps, terminal, tapPosition);
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.regulationMode = new TIntArrayList(stateArraySize);
        this.regulationValue = new TFloatArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.regulationMode.add(regulationMode.ordinal());
            this.regulationValue.add(regulationValue);
        }
    }

    @Override
    protected NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public RegulationMode getRegulationMode() {
        return RegulationMode.values()[regulationMode.get(network.get().getStateIndex())];
    }

    @Override
    public PhaseTapChanger setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), getTerminal(), getNetwork());
        this.regulationMode.set(network.get().getStateIndex(), regulationMode.ordinal());
        return this;
    }

    @Override
    public float getRegulationValue() {
        return regulationValue.get(network.get().getStateIndex());
    }

    @Override
    public PhaseTapChanger setRegulationValue(float regulationValue) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), regulationValue, getTerminal(), getNetwork());
        this.regulationValue.set(network.get().getStateIndex(), regulationValue);
        return this;
    }

    @Override
    public void remove() {
        parent.setPhaseTapChanger(null);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        regulationMode.ensureCapacity(regulationMode.size() + number);
        regulationValue.ensureCapacity(regulationValue.size() + number);
        for (int i = 0; i < number; i++) {
            regulationMode.add(regulationMode.get(sourceIndex));
            regulationValue.add(regulationValue.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        regulationMode.remove(regulationMode.size() - number, number);
        regulationValue.remove(regulationValue.size() - number, number);
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
            regulationMode.set(index, regulationMode.get(sourceIndex));
            regulationValue.set(index, regulationValue.get(sourceIndex));
        }
    }

    @Override
    protected String getTapChangerAttribute() {
        return "phaseTapChanger";
    }
}
