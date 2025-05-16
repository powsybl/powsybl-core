/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.ValidationUtil;
import com.powsybl.commons.ref.Ref;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
abstract class AbstractTapChanger<H extends TapChangerParent, C extends AbstractTapChanger<H, C, S>, S extends TapChangerStepImpl<S>> implements MultiVariantObject {

    protected final Ref<? extends VariantManagerHolder> network;

    protected final H parent;

    protected int lowTapPosition;

    protected Integer relativeNeutralPosition;

    protected List<S> steps;

    private final String type;

    protected final RegulatingPoint regulatingPoint;

    // attributes depending on the variant

    protected final ArrayList<Integer> tapPosition;

    protected final TDoubleArrayList targetDeadband;

    protected AbstractTapChanger(H parent,
                                 int lowTapPosition, List<S> steps, TerminalExt regulationTerminal,
                                 Integer tapPosition, boolean regulating, double targetDeadband, String type) {
        // The Ref object should be the one corresponding to the subnetwork of the tap changer holder
        // (to avoid errors when the subnetwork is detached)
        this.network = parent.getParentNetwork().getRootNetworkRef();
        this.parent = parent;
        this.lowTapPosition = lowTapPosition;
        this.steps = steps;
        steps.forEach(s -> s.setParent(this));
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        regulatingPoint = createRegulatingPoint(variantArraySize, regulating);
        regulatingPoint.setRegulatingTerminal(regulationTerminal);
        this.tapPosition = new ArrayList<>(variantArraySize);
        this.targetDeadband = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.tapPosition.add(tapPosition);
            this.targetDeadband.add(targetDeadband);
        }
        this.type = Objects.requireNonNull(type);
        relativeNeutralPosition = getRelativeNeutralPosition();
    }

    protected abstract RegulatingPoint createRegulatingPoint(int variantArraySize, boolean regulating);

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
            throwIncorrectTapPosition(tapPosition, getHighTapPosition());
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
        ValidationUtil.throwExceptionOrIgnore(parent, "tap position has been unset", n.getMinValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        Integer oldValue = this.tapPosition.set(variantIndex, null);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".tapPosition", variantId, oldValue, null);
        return (C) this;
    }

    public S getStep(int tapPosition) {
        if (tapPosition < lowTapPosition || tapPosition > getHighTapPosition()) {
            throwIncorrectTapPosition(tapPosition, getHighTapPosition());
        }
        return steps.get(tapPosition - lowTapPosition);
    }

    protected C setSteps(List<S> steps) {
        if (steps == null || steps.isEmpty()) {
            throw new ValidationException(parent, "a tap changer shall have at least one step");
        }
        steps.forEach(step -> step.validate(parent));

        // We check if the tap position is still correct
        int newHighTapPosition = lowTapPosition + steps.size() - 1;
        if (getTapPosition() > newHighTapPosition) {
            throwIncorrectTapPosition(getTapPosition(), newHighTapPosition);
        }

        this.steps = steps;
        this.relativeNeutralPosition = getRelativeNeutralPosition();
        return (C) this;
    }

    public S getCurrentStep() {
        Integer position = tapPosition.get(network.get().getVariantIndex());
        if (position == null) {
            return null;
        }
        return getStep(position);
    }

    public boolean isRegulating() {
        return regulatingPoint.isRegulating(network.get().getVariantIndex());
    }

    public C setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        int variantIndex = network.get().getVariantIndex();
        ValidationUtil.checkTargetDeadband(parent, type, regulating, targetDeadband.get(variantIndex), n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        boolean oldValue = regulatingPoint.setRegulating(variantIndex, regulating);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulating", variantId, oldValue, regulating);
        return (C) this;
    }

    public TerminalExt getRegulationTerminal() {
        return regulatingPoint.getRegulatingTerminal();
    }

    public C setRegulationTerminal(Terminal regulationTerminal) {
        if (regulationTerminal != null && ((TerminalExt) regulationTerminal).getVoltageLevel().getNetwork() != getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        Terminal oldValue = regulatingPoint.getRegulatingTerminal();
        regulatingPoint.setRegulatingTerminal((TerminalExt) regulationTerminal);
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".regulationTerminal", oldValue, regulationTerminal);
        return (C) this;
    }

    public double getTargetDeadband() {
        return targetDeadband.get(network.get().getVariantIndex());
    }

    public C setTargetDeadband(double targetDeadband) {
        int variantIndex = network.get().getVariantIndex();
        NetworkImpl n = getNetwork();
        ValidationUtil.checkTargetDeadband(parent, type, regulatingPoint.isRegulating(variantIndex),
                targetDeadband, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        double oldValue = this.targetDeadband.set(variantIndex, targetDeadband);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        n.getListeners().notifyUpdate(parent.getTransformer(), () -> getTapChangerAttribute() + ".targetDeadband", variantId, oldValue, targetDeadband);
        return (C) this;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        targetDeadband.ensureCapacity(targetDeadband.size() + number);
        tapPosition.ensureCapacity(tapPosition.size() + number);
        for (int i = 0; i < number; i++) {
            tapPosition.add(tapPosition.get(sourceIndex));
            targetDeadband.add(targetDeadband.get(sourceIndex));
        }
        regulatingPoint.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
    }

    @Override
    public void reduceVariantArraySize(int number) {
        List<Integer> tmpInt = new ArrayList<>(tapPosition.subList(0, tapPosition.size() - number));
        tapPosition.clear();
        tapPosition.addAll(tmpInt);
        targetDeadband.remove(targetDeadband.size() - number, number);
        regulatingPoint.reduceVariantArraySize(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        regulatingPoint.deleteVariantArrayElement(index);
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        for (int index : indexes) {
            tapPosition.set(index, tapPosition.get(sourceIndex));
            targetDeadband.set(index, targetDeadband.get(sourceIndex));
        }
        regulatingPoint.allocateVariantArrayElement(indexes, sourceIndex);
    }

    private void throwIncorrectTapPosition(int tapPosition, int highTapPosition) {
        throw new ValidationException(parent, "incorrect tap position "
            + tapPosition + " [" + lowTapPosition + ", " + highTapPosition
            + "]");
    }

    public void remove() {
        regulatingPoint.remove();
    }
}
