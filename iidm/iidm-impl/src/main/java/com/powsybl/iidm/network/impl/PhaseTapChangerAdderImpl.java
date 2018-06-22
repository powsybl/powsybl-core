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

    private final TwoWindingsTransformerImpl transformer;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<PhaseTapChangerStepImpl> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private boolean regulating = false;

    private TerminalExt regulationTerminal;

    class StepAdderImpl implements StepAdder {

        private double alpha = Double.NaN;

        private double rho = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = Double.NaN;

        private double b = Double.NaN;

        @Override
        public StepAdder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            if (Double.isNaN(alpha)) {
                throw new ValidationException(transformer, "step alpha is not set");
            }
            if (Double.isNaN(rho)) {
                throw new ValidationException(transformer, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(transformer, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(transformer, "step x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(transformer, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(transformer, "step b is not set");
            }
            PhaseTapChangerStepImpl step = new PhaseTapChangerStepImpl(alpha, rho, r, x, g, b);
            steps.add(step);
            return PhaseTapChangerAdderImpl.this;
        }

    }

    PhaseTapChangerAdderImpl(TwoWindingsTransformerImpl transformer) {
        this.transformer = transformer;
    }

    NetworkImpl getNetwork() {
        return transformer.getNetwork();
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
            throw new ValidationException(transformer, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(transformer, "a phase tap changer shall have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(transformer, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkPhaseTapChangerRegulation(transformer, regulationMode, regulationValue, regulating, regulationTerminal, getNetwork());
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(transformer, lowTapPosition, steps, regulationTerminal, tapPosition, regulating, regulationMode, regulationValue);
        transformer.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
