/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseTapChangerAdderImpl extends AbstractTapChangerAdderImpl<PhaseTapChangerAdderImpl, PhaseTapChangerParent, PhaseTapChanger, PhaseTapChangerStepImpl> implements PhaseTapChangerAdder {

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.CURRENT_LIMITER;

    class StepAdderImpl implements PhaseTapChangerAdder.StepAdder {

        private double alpha = Double.NaN;

        private double rho = 1.0;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        @Override
        public PhaseTapChangerAdder.StepAdder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public PhaseTapChangerAdder.StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            PhaseTapChangerStepImpl step = new PhaseTapChangerStepImpl(steps.size(), alpha, rho, r, x, g, b);
            step.validate(parent);
            steps.add(step);
            return PhaseTapChangerAdderImpl.this;
        }

    }

    PhaseTapChangerAdderImpl(PhaseTapChangerParent parent) {
        super(parent, true);
    }

    @Override
    public PhaseTapChangerAdder setRegulationMode(PhaseTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    protected PhaseTapChanger createTapChanger(PhaseTapChangerParent parent, int lowTapPosition, List<PhaseTapChangerStepImpl> steps, TerminalExt regulationTerminal, Integer tapPosition, Integer solvedTapPosition, boolean regulating, boolean loadTapChangingCapabilities, double regulationValue, double targetDeadband) {
        PhaseTapChangerImpl tapChanger = new PhaseTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities, tapPosition, solvedTapPosition, regulating, regulationMode, regulationValue, targetDeadband);
        parent.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

    @Override
    protected PhaseTapChangerAdderImpl self() {
        return this;
    }

    @Override
    protected ValidationLevel checkTapChangerRegulation(PhaseTapChangerParent parent, double regulationValue, boolean regulating, boolean loadTapChangingCapabilities, TerminalExt regulationTerminal) {
        return ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, regulating, loadTapChangingCapabilities,
                regulationTerminal, getNetwork(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    protected String getValidableType() {
        return "phase tap changer";
    }
}
