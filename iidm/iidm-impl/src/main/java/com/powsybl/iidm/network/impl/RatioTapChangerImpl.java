/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RatioTapChangerImpl extends AbstractTapChanger<RatioTapChangerParent, RatioTapChangerImpl, RatioTapChangerStepImpl> implements RatioTapChanger {

    private boolean loadTapChangingCapabilities;

    // attributes depending on the variant

    private final TDoubleArrayList targetV;

    RatioTapChangerImpl(RatioTapChangerParent parent, int lowTapPosition,
                        List<RatioTapChangerStepImpl> steps, TerminalExt regulationTerminal, boolean loadTapChangingCapabilities,
                        Integer tapPosition, Boolean regulating, double targetV, double targetDeadband) {
        super(parent.getNetwork().getRef(), parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating, targetDeadband, "ratio tap changer");
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.targetV = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.targetV.add(targetV);
        }
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, variantId, oldValue, newValue);
    }

    @Override
    protected Integer getRelativeNeutralPosition() {
        for (int i = 0; i < steps.size(); i++) {
            RatioTapChangerStepImpl step = steps.get(i);
            if (step.getRho() == 1) {
                return i;
            }
        }
        return null;
    }

    @Override
    public Optional<RatioTapChangerStep> getNeutralStep() {
        return relativeNeutralPosition != null ? Optional.of(steps.get(relativeNeutralPosition)) : Optional.empty();
    }

    @Override
    public RatioTapChangerImpl setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, loadTapChangingCapabilities,
                regulationTerminal, getTargetV(), n, n.getMinValidationLevel());
        Set<TapChanger<?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
                n.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        n.invalidateValidationLevel();
        return super.setRegulating(regulating);
    }

    @Override
    public RatioTapChangerImpl unsetRegulating() {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, null, loadTapChangingCapabilities,
                regulationTerminal, getTargetV(), n, n.getMinValidationLevel());
        Set<TapChanger<?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, null,
                n.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0);
        n.invalidateValidationLevel();
        return super.unsetRegulating();
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return loadTapChangingCapabilities;
    }

    @Override
    public RatioTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, findRegulatingStatus().orElse(null), loadTapChangingCapabilities, regulationTerminal,
                getTargetV(), n, n.getMinValidationLevel());
        boolean oldValue = this.loadTapChangingCapabilities;
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, loadTapChangingCapabilities);
        return this;
    }

    @Override
    public double getTargetV() {
        return targetV.get(network.get().getVariantIndex());
    }

    @Override
    public RatioTapChangerImpl setTargetV(double targetV) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, findRegulatingStatus().orElse(null), loadTapChangingCapabilities, regulationTerminal,
                targetV, n, n.getMinValidationLevel());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.targetV.set(variantIndex, targetV);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".targetV", variantId, oldValue, targetV);
        return this;
    }

    @Override
    public RatioTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkRatioTapChangerRegulation(parent, findRegulatingStatus().orElse(null),
                loadTapChangingCapabilities, regulationTerminal, getTargetV(), n, n.getMinValidationLevel());
        n.invalidateValidationLevel();
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public void remove() {
        parent.setRatioTapChanger(null);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        targetV.ensureCapacity(targetV.size() + number);
        for (int i = 0; i < number; i++) {
            targetV.add(targetV.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        targetV.remove(targetV.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, final int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            targetV.set(index, targetV.get(sourceIndex));
        }
    }

    @Override
    protected String getTapChangerAttribute() {
        return "ratio" + parent.getTapChangerAttribute();
    }

    @Override
    public Map<Integer, RatioTapChangerStep> getAllSteps() {
        Map<Integer, RatioTapChangerStep> allSteps = new HashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            allSteps.put(i + lowTapPosition, steps.get(i));
        }
        return allSteps;
    }
}
