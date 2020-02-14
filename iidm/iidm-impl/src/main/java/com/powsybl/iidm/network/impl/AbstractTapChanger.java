/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

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

    protected final TBooleanArrayList regulating;

    protected final TDoubleArrayList targetDeadband;

    protected AbstractTapChanger(Ref<? extends VariantManagerHolder> network, H parent,
                                 int lowTapPosition, List<S> steps, TerminalExt regulationTerminal,
                                 int tapPosition, boolean regulating, double targetDeadband) {
        this.network = network;
        this.parent = parent;
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        steps.stream().forEach(s -> s.setParent(this));
        this.regulationTerminal = regulationTerminal;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.tapPosition = new TIntArrayList(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.tapPosition.add(tapPosition);
            this.regulating.add(regulating);
            this.targetDeadband.add(targetDeadband);
        }
    }

    protected abstract NetworkImpl getNetwork();

    public int getStepCount() {
        return steps.size();
    }

    public int getLowTapPosition() {
        return lowTapPosition;
    }

    public C setLowTapPosition(int lowTapPosition) {
        int oldValue = this.lowTapPosition;
        this.lowTapPosition = lowTapPosition;
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".lowTapPosition", oldValue, lowTapPosition);
        int variantIndex = network.get().getVariantIndex();
        this.tapPosition.set(variantIndex, getTapPosition() + (this.lowTapPosition - oldValue));
        return (C) this;
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
        int variantIndex = network.get().getVariantIndex();
        int oldValue = this.tapPosition.set(variantIndex, tapPosition);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, tapPosition);
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
        int variantIndex = network.get().getVariantIndex();
        boolean oldValue = this.regulating.set(variantIndex, regulating);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        return (C) this;
    }

    public TerminalExt getRegulationTerminal() {
        return regulationTerminal;
    }

    public C setRegulationTerminal(Terminal regulationTerminal) {
        if (regulationTerminal != null && ((TerminalExt) regulationTerminal).getVoltageLevel().getNetwork() != getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        Terminal oldValue = this.regulationTerminal;
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulationTerminal", oldValue, regulationTerminal);
        return (C) this;
    }

    public double getTargetDeadband() {
        return targetDeadband.get(network.get().getVariantIndex());
    }

    public C setTargetDeadband(double targetDeadband) {
        if (!Double.isNaN(targetDeadband) && targetDeadband < 0) {
            throw new ValidationException(parent, "Unexpected value for target deadband of phase tap changer: " + targetDeadband);
        }
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        return (C) this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        regulating.ensureCapacity(regulating.size() + number);
        tapPosition.ensureCapacity(tapPosition.size() + number);
        for (int i = 0; i < number; i++) {
            regulating.add(regulating.get(sourceIndex));
            tapPosition.add(tapPosition.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        regulating.remove(regulating.size() - number, number);
        tapPosition.remove(tapPosition.size() - number, number);
        targetDeadband.remove(targetDeadband.size() - number, number);
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
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
        }
    }

}
