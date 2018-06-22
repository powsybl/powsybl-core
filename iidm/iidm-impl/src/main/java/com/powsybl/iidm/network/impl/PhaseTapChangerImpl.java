/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Terminal;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerImpl extends AbstractTapChanger<TwoWindingsTransformerImpl, PhaseTapChangerImpl, PhaseTapChangerStepImpl>
                          implements PhaseTapChanger {

    private RegulationMode regulationMode;

    // attributes depending on the state

    private final TDoubleArrayList regulationValue;

    PhaseTapChangerImpl(TwoWindingsTransformerImpl parent, int lowTapPosition,
                        List<PhaseTapChangerStepImpl> steps, TerminalExt regulationTerminal,
                        int tapPosition, boolean regulating, RegulationMode regulationMode, double regulationValue) {
        super(parent.getNetwork().getRef(), parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating);
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.regulationMode = regulationMode;
        this.regulationValue = new TDoubleArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.regulationValue.add(regulationValue);
        }
    }

    @Override
    protected NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public RegulationMode getRegulationMode() {
        return regulationMode;
    }

    @Override
    public PhaseTapChangerImpl setRegulationMode(RegulationMode regulationMode) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(), getRegulationTerminal(), getNetwork());
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public double getRegulationValue() {
        return regulationValue.get(network.get().getStateIndex());
    }

    @Override
    public PhaseTapChangerImpl setRegulationValue(double regulationValue) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, isRegulating(), getRegulationTerminal(), getNetwork());
        this.regulationValue.set(network.get().getStateIndex(), regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(), regulationTerminal, getNetwork());
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public void remove() {
        parent.setPhaseTapChanger(null);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        regulationValue.ensureCapacity(regulationValue.size() + number);
        for (int i = 0; i < number; i++) {
            regulationValue.add(regulationValue.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
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
            regulationValue.set(index, regulationValue.get(sourceIndex));
        }
    }

    @Override
    protected String getTapChangerAttribute() {
        return "phaseTapChanger";
    }
}
