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

import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, S>, S extends TapChangerStepImpl<S>> implements MultiVariantObject {

    protected final Ref<? extends VariantManagerHolder> network;

    protected final H parent;

    protected int lowTapPosition;

    protected final Integer relativeNeutralPosition;

    protected final List<S> steps;

    private final String type;

    protected TerminalExt regulationTerminal;

    // attributes depending on the variant

    protected final ArrayList<Integer> tapPosition;

    protected final TBooleanArrayList regulating;

    protected final TDoubleArrayList targetDeadband;

    protected AbstractTapChanger(Ref<? extends VariantManagerHolder> network, H parent,
                                 int lowTapPosition, List<S> steps, TerminalExt regulationTerminal,
                                 Integer tapPosition, boolean regulating, double targetDeadband, String type) {
        this.network = network;
        this.parent = parent;
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        steps.forEach(s -> s.setParent(this));
        this.regulationTerminal = regulationTerminal;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.tapPosition = new ArrayList<>(variantArraySize);
        this.regulating = new TBooleanArrayList(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.tapPosition.add(tapPosition);
            this.regulating.add(regulating);
            this.targetDeadband.add(targetDeadband);
        }
        this.type = Objects.requireNonNull(type);
        relativeNeutralPosition = getRelativeNeutralPosition();
    }

    protected NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

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
        int variantIndex = network.get().getVariantIndex();
        Integer position = tapPosition.get(network.get().getVariantIndex());
        this.tapPosition.set(variantIndex, position != null ? position + (this.lowTapPosition - oldValue) : null);
        return (C) this;
    }

    public int getHighTapPosition() {
        return lowTapPosition + steps.size() - 1;
    }

    public int getTapPosition() {
        Integer position = tapPosition.get(network.get().getVariantIndex());
        if (position == null) {
            throw ValidationUtil.createUndefinedValueGetterException();
        }
        return position;
    }

    public OptionalInt findTapPosition() {
        Integer position = tapPosition.get(network.get().getVariantIndex());
        return position == null ? OptionalInt.empty() : OptionalInt.of(position);
    }

    public OptionalInt getNeutralPosition() {
        return relativeNeutralPosition != null ? OptionalInt.of(lowTapPosition + relativeNeutralPosition) : OptionalInt.empty();
    }

    protected abstract String getTapChangerAttribute();

    public C setTapPosition(int tapPosition) {
        NetworkImpl n = getNetwork();
        if (tapPosition < lowTapPosition
                || tapPosition > getHighTapPosition()) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + getHighTapPosition() + "]");
        }
        int variantIndex = n.getVariantIndex();
        Integer oldValue = this.tapPosition.set(variantIndex, tapPosition);
        String variantId = n.getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        parent.getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, tapPosition);
        return (C) this;
    }

    public C unsetTapPosition() {
        NetworkImpl n = getNetwork();
        ValidationUtil.throwExceptionOrLogError(parent, "tap position has been unset", n.getMinValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        Integer oldValue = this.tapPosition.set(variantIndex, null);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, null);
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
        Integer position = tapPosition.get(network.get().getVariantIndex());
        if (position == null) {
            return null;
        }
        return getStep(position);
    }

    public boolean isRegulating() {
        return regulating.get(network.get().getVariantIndex());
    }

    public C setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, regulating, targetDeadband.get(variantIndex), n.getMinValidationLevel());
        boolean oldValue = this.regulating.set(variantIndex, regulating);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
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
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulationTerminal", oldValue, regulationTerminal);
        return (C) this;
    }

    public double getTargetDeadband() {
        return targetDeadband.get(network.get().getVariantIndex());
    }

    public C setTargetDeadband(double targetDeadband) {
        int variantIndex = network.get().getVariantIndex();
        NetworkImpl n = getNetwork();
        ValidationUtil.checkTargetDeadband(parent, type, this.regulating.get(variantIndex),
                targetDeadband, n.getMinValidationLevel());
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        return (C) this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        regulating.ensureCapacity(regulating.size() + number);
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        tapPosition.ensureCapacity(tapPosition.size() + number);
        for (int i = 0; i < number; i++) {
            regulating.add(regulating.get(sourceIndex));
            tapPosition.add(tapPosition.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        List<Integer> tmpInt = new ArrayList<>(tapPosition.subList(0, tapPosition.size() - number));
        tapPosition.clear();
        tapPosition.addAll(tmpInt);
        regulating.remove(regulating.size() - number, number);
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
