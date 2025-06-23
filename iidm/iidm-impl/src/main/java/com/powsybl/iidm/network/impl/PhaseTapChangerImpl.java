/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import gnu.trove.list.array.TDoubleArrayList;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseTapChangerImpl extends AbstractTapChanger<PhaseTapChangerParent, PhaseTapChangerImpl, PhaseTapChangerStepImpl>
                          implements PhaseTapChanger {

    private RegulationMode regulationMode;

    // attributes depending on the variant

    private final TDoubleArrayList regulationValue;

    PhaseTapChangerImpl(PhaseTapChangerParent parent, int lowTapPosition,
                        List<PhaseTapChangerStepImpl> steps, TerminalExt regulationTerminal, boolean loadTapChangingCapabilities,
                        Integer tapPosition, Boolean regulating, RegulationMode regulationMode, double regulationValue, double targetDeadband) {
        super(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities, tapPosition, regulating, targetDeadband, "phase tap changer");
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.regulationMode = regulationMode;
        this.regulationValue = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.regulationValue.add(regulationValue);
        }
    }

    protected void notifyUpdate(Supplier<String> attribute, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, oldValue, newValue);
    }

    protected void notifyUpdate(Supplier<String> attribute, String variantId, Object oldValue, Object newValue) {
        getNetwork().getListeners().notifyUpdate(parent.getTransformer(), attribute, variantId, oldValue, newValue);
    }

    @Override
    public PhaseTapChangerStepsReplacerImpl stepsReplacer() {
        return new PhaseTapChangerStepsReplacerImpl(this);
    }

    @Override
    public Optional<PhaseTapChangerStep> getNeutralStep() {
        return relativeNeutralPosition != null ? Optional.of(steps.get(relativeNeutralPosition)) : Optional.empty();
    }

    @Override
    protected RegulatingPoint createRegulatingPoint(int variantArraySize, boolean regulating) {
        return new RegulatingPoint(parent.getTransformer().getId(), () -> null, variantArraySize, regulating, false);
    }

    @Override
    protected Integer getRelativeNeutralPosition() {
        for (int i = 0; i < steps.size(); i++) {
            PhaseTapChangerStepImpl step = steps.get(i);
            if (step.getRho() == 1 && step.getAlpha() == 0) {
                return i;
            }
        }
        return null;
    }

    @Override
    public PhaseTapChangerImpl setRegulating(boolean regulating) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkPhaseTapChangerRegulation(parent, getRegulationMode(), getRegulationValue(), regulating,
                hasLoadTapChangingCapabilities(), getRegulationTerminal(), n, n.getMinValidationLevel(),
                n.getReportNodeContext().getReportNode());
        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getPhaseTapChanger());
        ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers,
                regulating, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        n.invalidateValidationLevel();
        return super.setRegulating(regulating);
    }

    @Override
    public RegulationMode getRegulationMode() {
        return regulationMode;
    }

    @Override
    public PhaseTapChangerImpl setRegulationMode(RegulationMode regulationMode) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(),
                isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), n, n.getMinValidationLevel(),
                n.getReportNodeContext().getReportNode());
        RegulationMode oldValue = this.regulationMode;
        this.regulationMode = regulationMode;
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationMode", oldValue, regulationMode);
        return this;
    }

    @Override
    public double getRegulationValue() {
        return regulationValue.get(network.get().getVariantIndex());
    }

    @Override
    public PhaseTapChangerImpl setRegulationValue(double regulationValue) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue,
                isRegulating(), hasLoadTapChangingCapabilities(), getRegulationTerminal(), n, n.getMinValidationLevel(),
                n.getReportNodeContext().getReportNode());
        int variantIndex = network.get().getVariantIndex();
        double oldValue = this.regulationValue.set(variantIndex, regulationValue);
        String variantId = network.get().getVariantManager().getVariantId(variantIndex);
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".regulationValue", variantId, oldValue, regulationValue);
        return this;
    }

    @Override
    public PhaseTapChangerImpl setRegulationTerminal(Terminal regulationTerminal) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(),
                hasLoadTapChangingCapabilities(), regulationTerminal, n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        n.invalidateValidationLevel();
        return super.setRegulationTerminal(regulationTerminal);
    }

    @Override
    public boolean hasLoadTapChangingCapabilities() {
        return loadTapChangingCapabilities;
    }

    @Override
    public PhaseTapChangerImpl setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        NetworkImpl n = getNetwork();
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, getRegulationValue(), isRegulating(),
                loadTapChangingCapabilities, getRegulationTerminal(), n, n.getMinValidationLevel(), n.getReportNodeContext().getReportNode());
        boolean oldValue = this.loadTapChangingCapabilities;
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        n.invalidateValidationLevel();
        notifyUpdate(() -> getTapChangerAttribute() + ".loadTapChangingCapabilities", oldValue, loadTapChangingCapabilities);
        return this;
    }

    @Override
    public void remove() {
        super.remove();
        parent.setPhaseTapChanger(null);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        regulationValue.ensureCapacity(regulationValue.size() + number);
        for (int i = 0; i < number; i++) {
            regulationValue.add(regulationValue.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        regulationValue.remove(regulationValue.size() - number, number);
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
            regulationValue.set(index, regulationValue.get(sourceIndex));
        }
    }

    @Override
    protected String getTapChangerAttribute() {
        return "phase" + parent.getTapChangerAttribute();
    }

    @Override
    public Map<Integer, PhaseTapChangerStep> getAllSteps() {
        Map<Integer, PhaseTapChangerStep> allSteps = new HashMap<>();
        for (int i = 0; i < steps.size(); i++) {
            allSteps.put(i + lowTapPosition, steps.get(i));
        }
        return allSteps;
    }

}
