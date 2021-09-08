/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.iidm.network.impl.util.Ref;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, S>, S extends TapChangerStepImpl<S>> implements MultiVariantObject {

    protected final NetworkImpl network;

    protected final Ref<? extends VariantManagerHolder> networkRef;

    protected final H parent;

    protected int lowTapPosition;

    protected final Integer relativeNeutralPosition;

    protected final List<S> steps;

    private final String type;

    protected TerminalExt regulationTerminal;

    // attributes depending on the variant

    protected final TIntArrayList tapPosition;

    protected final TBooleanArrayList regulating;

    protected final TDoubleArrayList targetDeadband;

    protected AbstractTapChanger(Ref<? extends VariantManagerHolder> networkRef, H parent,
                                 int lowTapPosition, List<S> steps, TerminalExt regulationTerminal,
                                 Integer tapPosition, boolean regulating, double targetDeadband, String type) {
        this.networkRef = networkRef;
        this.parent = parent;
        network = parent.getNetwork();
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        steps.forEach(s -> s.setParent(this));
        this.regulationTerminal = regulationTerminal;
        int variantArraySize = networkRef.get().getVariantManager().getVariantArraySize();
        this.tapPosition = new TIntArrayList(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.tapPosition.add(tapPosition != null ? tapPosition : Integer.MIN_VALUE);
            this.regulating.add(regulating);
            this.targetDeadband.add(targetDeadband);
        }
        this.type = Objects.requireNonNull(type);
        relativeNeutralPosition = getRelativeNeutralPosition();
    }

    protected abstract NetworkImpl getNetwork();

    protected abstract Integer getRelativeNeutralPosition();

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
        int variantIndex = networkRef.get().getVariantIndex();
        this.tapPosition.set(variantIndex, getTapPosition() + (this.lowTapPosition - oldValue));
        return (C) this;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public int getTapPosition() {
        int position = tapPosition.get(networkRef.get().getVariantIndex());
        if (position == Integer.MIN_VALUE) {
            throw new PowsyblException("Undefined tap position, use getTapPositionAsInteger");
        }
        return position;
    }

    public Integer getTapPositionAsInteger() {
        int position = tapPosition.get(networkRef.get().getVariantIndex());
        if (position == Integer.MIN_VALUE) {
            return null;
        }
        return position;
    }

    public OptionalInt getNeutralPosition() {
        return relativeNeutralPosition != null ? OptionalInt.of(lowTapPosition + relativeNeutralPosition) : OptionalInt.empty();
    }

    protected abstract String getTapChangerAttribute();

    public C setTapPosition(int tapPosition) {
        if (tapPosition < lowTapPosition
                || tapPosition > getHighTapPosition()) {
            ValidationUtil.throwExceptionOrLogError(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + getHighTapPosition() + "]", getNetwork().areValidationChecksEnabled());
        }
        int variantIndex = networkRef.get().getVariantIndex();
        int oldValue = this.tapPosition.set(variantIndex, tapPosition);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, tapPosition);
        network.uncheckValidationStatusIfDisabledCheck();
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
        return regulating.get(networkRef.get().getVariantIndex());
    }

    public C setRegulating(boolean regulating) {
        int variantIndex = networkRef.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, regulating, targetDeadband.get(variantIndex), getNetwork().areValidationChecksEnabled());
        boolean oldValue = this.regulating.set(variantIndex, regulating);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        network.uncheckValidationStatusIfDisabledCheck();
        return (C) this;
    }

    public TerminalExt getRegulationTerminal() {
        return regulationTerminal;
    }

    public C setRegulationTerminal(Terminal regulationTerminal) {
        if (regulationTerminal != null && ((TerminalExt) regulationTerminal).getVoltageLevel().getNetwork() != getNetwork()) {
            ValidationUtil.throwExceptionOrLogError(parent, "regulation terminal is not part of the network", getNetwork().areValidationChecksEnabled());
        }
        Terminal oldValue = this.regulationTerminal;
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulationTerminal", oldValue, regulationTerminal);
        network.uncheckValidationStatusIfDisabledCheck();
        return (C) this;
    }

    public double getTargetDeadband() {
        return targetDeadband.get(networkRef.get().getVariantIndex());
    }

    public C setTargetDeadband(double targetDeadband) {
        int variantIndex = networkRef.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, this.regulating.get(variantIndex), targetDeadband, getNetwork().areValidationChecksEnabled());
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        network.uncheckValidationStatusIfDisabledCheck();
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
