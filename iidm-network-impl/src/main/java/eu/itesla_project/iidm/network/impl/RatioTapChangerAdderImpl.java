/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.RatioTapChanger;
import eu.itesla_project.iidm.network.RatioTapChangerAdder;
import eu.itesla_project.iidm.network.Terminal;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    private final RatioTapChangerParent parent;

    private int lowStepPosition = 0;

    private Integer currentStepPosition;

    private final List<RatioTapChangerStepImpl> steps = new ArrayList<>();

    private Boolean loadTapChangingCapabilities;

    private Boolean regulating;

    private float targetV = Float.NaN;

    private TerminalExt terminal;

    class StepAdderImpl implements StepAdder {

        private float rho = Float.NaN;

        private float r = Float.NaN;

        private float x = Float.NaN;

        private float g = Float.NaN;

        private float b = Float.NaN;

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
        public RatioTapChangerAdder endStep() {
            if (Float.isNaN(rho)) {
                throw new ValidationException(parent, "step rho is not set");
            }
            if (Float.isNaN(r)) {
                throw new ValidationException(parent, "step r is not set");
            }
            if (Float.isNaN(x)) {
                throw new ValidationException(parent, "step x is not set");
            }
            if (Float.isNaN(g)) {
                throw new ValidationException(parent, "step g is not set");
            }
            if (Float.isNaN(b)) {
                throw new ValidationException(parent, "step b is not set");
            }
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(rho, r, x, g, b);
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
    public RatioTapChangerAdder setLowStepPosition(int lowStepPosition) {
        this.lowStepPosition = lowStepPosition;
        return this;
    }

    @Override
    public RatioTapChangerAdder setCurrentStepPosition(int currentStepPosition) {
        this.currentStepPosition = currentStepPosition;
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
    public RatioTapChangerAdder setTargetV(float targetV) {
        this.targetV = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTerminal(Terminal terminal) {
        this.terminal = (TerminalExt) terminal;
        return this;
    }

    @Override
    public StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        if (currentStepPosition == null) {
            throw new ValidationException(parent, "current step position is not set");
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "ratio tap changer should have at least one step");
        }
        int highStepPosition = lowStepPosition + steps.size() - 1;
        if (currentStepPosition < lowStepPosition || currentStepPosition > highStepPosition) {
            throw new ValidationException(parent, "incorrect current step position "
                    + currentStepPosition + " [" + lowStepPosition + ", "
                    + highStepPosition + "]");
        }
        if (loadTapChangingCapabilities == null) {
            throw new ValidationException(parent, "load tap changing capabilities is not set");
        }
        if (loadTapChangingCapabilities) {
            if (regulating == null) {
                throw new ValidationException(parent,
                        "a regulating status has to be set for a ratio tap changer with load tap changing capabilities");
            }
            if (regulating) {
                if (Float.isNaN(targetV)) {
                    throw new ValidationException(parent,
                            "a target voltage has to be set for a regulating ratio tap changer");
                }
                if (targetV <= 0) {
                    throw new ValidationException(parent, "bad target voltage " + targetV);
                }
                if (terminal == null) {
                    throw new ValidationException(parent,
                            "a regulation terminal has to be set for a regulating ratio tap changer");
                }
            }
        } else {
            if (regulating != null) {
                throw new ValidationException(parent,
                        "a regulating status is useless for a ratio tap changer without any load tap changing capabilities");
            }
            if (!Float.isNaN(targetV)) {
                throw new ValidationException(parent,
                        "a target voltage is useless for a ratio tap changer without any load tap changing capabilities");
            }
            if (terminal != null) {
                throw new ValidationException(parent,
                        "a regulation terminal is useless for a ratio tap changer without any load tap changing capabilities");
            }
        }
        if (terminal != null && terminal.getVoltageLevel().getNetwork() != getNetwork()) {
            throw new ValidationException(parent, "regulation terminal is not part of the network");
        }
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowStepPosition, steps, terminal, loadTapChangingCapabilities,
                                          currentStepPosition, regulating != null ? regulating : false, targetV);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
