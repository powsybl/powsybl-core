/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TIntArrayList;
import java.util.BitSet;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, S>, S extends TapChangerStepImpl<S>> implements MultiVariantObject {

    protected final Ref<? extends VariantManagerHolder> network;

    protected final H parent;

    protected int lowTapPosition;

    protected final List<S> steps;

    protected TerminalExt regulationTerminal;

    // attributes depending on the variant

    protected final TIntArrayList tapPosition;

    protected final BitSet regulating;

    protected AbstractTapChanger(Ref<? extends VariantManagerHolder> network, H parent,
                                 int lowTapPosition, List<S> steps, TerminalExt regulationTerminal,
                                 int tapPosition, boolean regulating) {
        this.network = network;
        this.parent = parent;
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        this.regulationTerminal = regulationTerminal;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.tapPosition = new TIntArrayList(variantArraySize);
        this.regulating = new BitSet(variantArraySize);
        this.regulating.set(0, variantArraySize, regulating);
        for (int i = 0; i < variantArraySize; i++) {
            this.tapPosition.add(tapPosition);
        }
    }

    protected abstract NetworkImpl getNetwork();

    public int getStepCount() {
        return steps.size();
    }

    public int getLowTapPosition() {
        return lowTapPosition;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public int getTapPosition() {
        return tapPosition.get(network.get().getVariantIndex());
    }

    protected abstract String getTapChangerAttribute();

    public C setTapPosition(int tapPosition) {
        if (tapPosition < lowTapPosition
                || tapPosition > getHighTapPosition()) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + getHighTapPosition() + "]");
        }
        int oldValue = this.tapPosition.set(network.get().getVariantIndex(), tapPosition);
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), getTapChangerAttribute() + ".tapPosition", oldValue, tapPosition);
        return (C) this;
    }

    public S getStep(int tapPosition) {
        if (tapPosition < lowTapPosition || tapPosition > getHighTapPosition()) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", " + getHighTapPosition()
                    + "]");
        }
        return steps.get(tapPosition - lowTapPosition);
    }

    public S getCurrentStep() {
        return getStep(getTapPosition());
    }

    public boolean isRegulating() {
        return regulating.get(network.get().getVariantIndex());
    }

    public C setRegulating(boolean regulating) {
        this.regulating.set(network.get().getVariantIndex(), regulating);
        return (C) this;
    }

    public TerminalExt getRegulationTerminal() {
        return regulationTerminal;
    }

    public C setRegulationTerminal(Terminal regulationTerminal) {
        if (regulationTerminal == null) {
            throw new ValidationException(parent, "regulation terminal is null");
        }
        if (((TerminalExt) regulationTerminal).getVoltageLevel().getNetwork() != getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return (C) this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        tapPosition.ensureCapacity(tapPosition.size() + number);
        for (int i = 0; i < number; i++) {
            regulating.set(initVariantArraySize + i, regulating.get(sourceIndex));
            tapPosition.add(tapPosition.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        tapPosition.remove(tapPosition.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            regulating.set(index, regulating.get(sourceIndex));
            tapPosition.set(index, tapPosition.get(sourceIndex));
        }
    }

}
