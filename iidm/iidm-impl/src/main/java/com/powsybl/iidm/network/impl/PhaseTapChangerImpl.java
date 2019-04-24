/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import java.util.List;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Terminal;

import gnu.trove.list.array.TDoubleArrayList;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerImpl extends AbstractTapChanger<PhaseTapChangerParent, PhaseTapChangerImpl, PhaseTapChangerStepImpl>
                          implements PhaseTapChanger {

    private RegulationMode regulationMode;

    // attributes depending on the variant

    private final TDoubleArrayList regulationValue;

    PhaseTapChangerImpl(PhaseTapChangerParent parent, int lowTapPosition,
                        List<PhaseTapChangerStepImpl> steps, TerminalExt regulationTerminal,
                        int tapPosition, boolean regulating, RegulationMode regulationMode, double regulationValue) {
        super(parent.getNetwork().getRef(), parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating);
        int variantArraySize = network.get().getVariantManager().getVariantArraySize();
        this.regulationMode = regulationMode;
        this.regulationValue = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
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
        return regulationValue.get(network.get().getVariantIndex());
    }

    @Override
    public PhaseTapChangerImpl setRegulationValue(double regulationValue) {
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, isRegulating(), getRegulationTerminal(), getNetwork());
        this.regulationValue.set(network.get().getVariantIndex(), regulationValue);
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
        return "phaseTapChanger";
    }
}
