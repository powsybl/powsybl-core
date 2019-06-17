/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerAdderImpl implements PhaseTapChangerAdder {

    private final PhaseTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<PhaseTapChangerStepImpl> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private boolean regulating = false;

    private TerminalExt regulationTerminal;

    class StepAdderImpl extends AbstractTapChangerStepAdder<StepAdderImpl> implements StepAdder {

        @Override
        public PhaseTapChangerAdderImpl endStep() {
            if (Double.isNaN(alpha)) {
                throw new ValidationException(parent, "step alpha is not set");
            }
            checkValues(parent);
            PhaseTapChangerStepImpl step = new PhaseTapChangerStepImpl(alpha, rho, r, x, g, b);
            steps.add(step);
            return PhaseTapChangerAdderImpl.this;
        }

    }

    PhaseTapChangerAdderImpl(PhaseTapChangerParent parent) {
        this.parent = parent;
    }

    NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public PhaseTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public PhaseTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(parent, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "a phase tap changer shall have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, regulating, regulationTerminal, getNetwork());
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating, regulationMode, regulationValue);
        parent.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
