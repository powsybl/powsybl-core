/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.PhaseTapChanger;
import gnu.trove.list.array.TFloatArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerImpl extends TapChangerImpl<TwoWindingsTransformerImpl, PhaseTapChangerImpl, PhaseTapChangerStepImpl>
                          implements PhaseTapChanger {

    // attributes depending on the state

    private final TFloatArrayList thresholdI;

    PhaseTapChangerImpl(TwoWindingsTransformerImpl parent, int lowStepPosition,
                        List<PhaseTapChangerStepImpl> steps, TerminalExt terminal,
                        int currentStepPosition, boolean regulating, float thresholdI) {
        super(parent.getNetwork().getRef(), parent, lowStepPosition, steps, terminal, currentStepPosition, regulating);
        int stateArraySize = network.get().getStateManager().getStateArraySize();
        this.thresholdI = new TFloatArrayList(stateArraySize);
        for (int i = 0; i < stateArraySize; i++) {
            this.thresholdI.add(thresholdI);
        }
    }

    @Override
    protected NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public PhaseTapChangerImpl setRegulating(boolean regulating) {
        if (Float.isNaN(getThresholdI())) {
            throw new ValidationException(parent,
                    "cannot change the regulating status if the threshold current is not set");
        }
        return super.setRegulating(regulating);
    }

    @Override
    public float getThresholdI() {
        return thresholdI.get(network.get().getStateIndex());
    }

    @Override
    public PhaseTapChanger setThresholdI(float thresholdI) {
        if (Float.isNaN(thresholdI)) {
            throw new ValidationException(parent, "invalid threshold current value (NaN)");
        }
        this.thresholdI.set(network.get().getStateIndex(), thresholdI);
        return this;
    }

    @Override
    public void remove() {
        parent.setPhaseTapChanger(null);
    }

    @Override
    public void extendStateArraySize(int initStateArraySize, int number, int sourceIndex) {
        super.extendStateArraySize(initStateArraySize, number, sourceIndex);
        thresholdI.ensureCapacity(thresholdI.size() + number);
        for (int i = 0; i < number; i++) {
            thresholdI.add(thresholdI.get(sourceIndex));
        }
    }

    @Override
    public void reduceStateArraySize(int number) {
        super.reduceStateArraySize(number);
        thresholdI.remove(thresholdI.size() - number, number);
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
            thresholdI.set(index, thresholdI.get(sourceIndex));
        }
    }

    @Override
    protected String getTapChangerAttribute() {
        return "phaseTapChanger";
    }
}
