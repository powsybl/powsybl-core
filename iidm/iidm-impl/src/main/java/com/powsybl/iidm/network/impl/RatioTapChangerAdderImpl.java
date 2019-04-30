/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    private final RatioTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<RatioTapChangerStepImpl> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private boolean regulating = false;

    private double targetV = Double.NaN;

    private TerminalExt regulationTerminal;

    class StepAdderImpl implements StepAdder {

        private double rho = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g1 = Double.NaN;

        private double b1 = Double.NaN;

        private double g2 = Double.NaN;

        private double b2 = Double.NaN;

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
        public StepAdder setG1(double g1) {
            this.g1 = g1;
            return this;
        }

        @Override
        public StepAdder setB1(double b1) {
            this.b1 = b1;
            return this;
        }

        @Override
        public StepAdder setG2(double g2) {
            this.g2 = g2;
            return this;
        }

        @Override
        public StepAdder setB2(double b2) {
            this.b2 = b2;
            return this;
        }

        @Override
        public RatioTapChangerAdder endStep() {
            if (Double.isNaN(rho)) {
                throw new ValidationException(parent, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(parent, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(parent, "step x is not set");
            }
            if (Double.isNaN(g1)) {
                throw new ValidationException(parent, "step g1 is not set");
            }
            if (Double.isNaN(b1)) {
                throw new ValidationException(parent, "step b1 is not set");
            }
            if (Double.isNaN(g2)) {
                throw new ValidationException(parent, "step g2 is not set");
            }
            if (Double.isNaN(b2)) {
                throw new ValidationException(parent, "step b2 is not set");
            }
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(rho, r, x, g1, b1, g2, b2);
            steps.add(step);
            return RatioTapChangerAdderImpl.this;
        }

    }

    RatioTapChangerAdderImpl(RatioTapChangerParent parent) {
        this.parent = parent;
    }

    NetworkImpl getNetwork() {
        return parent.getNetwork();
    }

    @Override
    public RatioTapChangerAdder setLowTapPosition(int lowTapPosition) {
        this.lowTapPosition = lowTapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTapPosition(int tapPosition) {
        this.tapPosition = tapPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setLoadTapChangingCapabilities(boolean loadTapChangingCapabilities) {
        this.loadTapChangingCapabilities = loadTapChangingCapabilities;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        if (tapPosition == null) {
            throw new ValidationException(parent, "tap position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "ratio tap changer should have at least one step");
        }
        int highTapPosition = lowTapPosition + steps.size() - 1;
        if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
            throw new ValidationException(parent, "incorrect tap position "
                    + tapPosition + " [" + lowTapPosition + ", "
                    + highTapPosition + "]");
        }
        ValidationUtil.checkRatioTapChangerRegulation(parent, loadTapChangingCapabilities, regulating, regulationTerminal, targetV, getNetwork());
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities,
                                          tapPosition, regulating, targetV);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
