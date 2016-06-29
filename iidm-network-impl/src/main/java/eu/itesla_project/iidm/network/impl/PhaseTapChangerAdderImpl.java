/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.PhaseTapChanger;
import eu.itesla_project.iidm.network.PhaseTapChangerAdder;
import eu.itesla_project.iidm.network.Terminal;
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

    private Boolean regulating;

    private float thresholdI = Float.NaN;

    private TerminalExt terminal;

    class StepAdderImpl implements StepAdder {

        private float alpha = Float.NaN;

        private float rho = Float.NaN;

        private float r = Float.NaN;

        private float x = Float.NaN;

        private float g = Float.NaN;

        private float b = Float.NaN;

        @Override
        public StepAdder setAlpha(float alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public StepAdder setRho(float rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public StepAdder setR(float r) {
            this.r = r;
            return this;
        }

        @Override
        public StepAdder setX(float x) {
            this.x = x;
            return this;
        }

        @Override
        public StepAdder setG(float g) {
            this.g = g;
            return this;
        }

        @Override
        public StepAdder setB(float b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerAdder endStep() {
            if (Float.isNaN(alpha)) {
                throw new ValidationException(transformer, "step alpha is not set");
            }
            if (Float.isNaN(rho)) {
                throw new ValidationException(transformer, "step rho is not set");
            }
            if (Float.isNaN(r)) {
                throw new ValidationException(transformer, "step r is not set");
            }
            if (Float.isNaN(x)) {
                throw new ValidationException(transformer, "step x is not set");
            }
            if (Float.isNaN(g)) {
                throw new ValidationException(transformer, "step g is not set");
            }
            if (Float.isNaN(b)) {
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
    public PhaseTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setThresholdI(float thresholdI) {
        this.thresholdI = thresholdI;
        return this;
    }

    @Override
    public PhaseTapChangerAdder setTerminal(Terminal terminal) {
        this.terminal = (TerminalExt) terminal;
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
        if (regulating == null) {
            throw new ValidationException(transformer, "regulating status is not set");
        }
        if (regulating) {
            if (Float.isNaN(thresholdI)) {
                throw new ValidationException(transformer, "a threshold current has to be set for a regulating phase tap changer");
            }
            if (terminal == null) {
                throw new ValidationException(transformer, "a regulation terminal has to be set for a regulating phase tap changer");
            }
            if (terminal.getVoltageLevel().getNetwork() != getNetwork()) {
                throw new ValidationException(transformer, "terminal is not part of the network");
            }
        }
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(transformer, lowTapPosition, steps, terminal,
                tapPosition, regulating, thresholdI);
        transformer.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
