/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.*;
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
        int position = tapPosition.get(networkRef.get().getVariantIndex());
        this.tapPosition.set(variantIndex, position != Integer.MIN_VALUE ? position + (this.lowTapPosition - oldValue) : Integer.MIN_VALUE);
        return (C) this;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public OptionalInt getTapPosition() {
        int position = tapPosition.get(networkRef.get().getVariantIndex());
        if (position == Integer.MIN_VALUE) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(position);
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
                    + getHighTapPosition() + "]", network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        }
        int variantIndex = networkRef.get().getVariantIndex();
        Integer oldValue = this.tapPosition.set(variantIndex, tapPosition);
        if (oldValue == Integer.MIN_VALUE) {
            oldValue = null;
        }
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, tapPosition);
        network.invalidate();
        return (C) this;
    }

    public C unsetTapPosition() {
        ValidationUtil.throwExceptionOrLogError(parent, "tap position has been unset", network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        int variantIndex = networkRef.get().getVariantIndex();
        Integer oldValue = this.tapPosition.set(variantIndex, Integer.MIN_VALUE);
        if (oldValue == Integer.MIN_VALUE) {
            oldValue = null;
        }
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, tapPosition);
        network.invalidate();
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
        int position = tapPosition.get(networkRef.get().getVariantIndex());
        if (position == Integer.MIN_VALUE) {
            return null;
        }
        return getStep(position);
    }

    public boolean isRegulating() {
        return regulating.get(networkRef.get().getVariantIndex());
    }

    public C setRegulating(boolean regulating) {
        int variantIndex = networkRef.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, regulating, targetDeadband.get(variantIndex), network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        boolean oldValue = this.regulating.set(variantIndex, regulating);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        network.invalidate();
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
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulationTerminal", oldValue, regulationTerminal);
        return (C) this;
    }

    public double getTargetDeadband() {
        return targetDeadband.get(networkRef.get().getVariantIndex());
    }

    public C setTargetDeadband(double targetDeadband) {
        int variantIndex = networkRef.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, this.regulating.get(variantIndex),
                targetDeadband, network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = networkRef.get().getVariantManager().getVariantId(variantIndex);
        network.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        network.invalidate();
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
