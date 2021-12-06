/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class PhaseTapChangerAdderImpl implements PhaseTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhaseTapChangerAdderImpl.class);

    private final PhaseTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<PhaseTapChangerStepImpl> steps = new ArrayList<>();

    private PhaseTapChanger.RegulationMode regulationMode = PhaseTapChanger.RegulationMode.FIXED_TAP;

    private double regulationValue = Double.NaN;

    private boolean regulating = false;

    private double targetDeadband = Double.NaN;

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
                throw new ValidationException(parent, "step alpha is not set");
            }
            if (Double.isNaN(rho)) {
                throw new ValidationException(parent, "step rho is not set");
            }
            if (Double.isNaN(r)) {
                throw new ValidationException(parent, "step r is not set");
            }
            if (Double.isNaN(x)) {
                throw new ValidationException(parent, "step x is not set");
            }
            if (Double.isNaN(g)) {
                throw new ValidationException(parent, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(parent, "step b is not set");
            }
            PhaseTapChangerStepImpl step = new PhaseTapChangerStepImpl(steps.size(), alpha, rho, r, x, g, b);
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
    public PhaseTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
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
        NetworkImpl network = getNetwork();
        if (tapPosition == null) {
            ValidationUtil.throwExceptionOrLogError(parent, "tap position is not set", network.getMinValidationLevel());
            network.setValidationLevelIfGreaterThan(ValidationLevel.SCADA);
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "a phase tap changer shall have at least one step");
        }
        if (tapPosition != null) {
            int highTapPosition = lowTapPosition + steps.size() - 1;
            if (tapPosition < lowTapPosition || tapPosition > highTapPosition) {
                ValidationUtil.throwExceptionOrLogError(parent, "incorrect tap position "
                        + tapPosition + " [" + lowTapPosition + ", "
                        + highTapPosition + "]", network.getMinValidationLevel());
                network.setValidationLevelIfGreaterThan(ValidationLevel.SCADA);
            }
        }
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, regulating,
                regulationTerminal, network, network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(parent, "phase tap changer", regulating,
                targetDeadband, network.getMinValidationLevel()));
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating, regulationMode, regulationValue, targetDeadband);

        Set<TapChanger> tapChangers = new HashSet<>();
        tapChangers.addAll(parent.getAllTapChangers());
        tapChangers.remove(parent.getPhaseTapChanger());
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers,
                regulating, network.getMinValidationLevel().compareTo(ValidationLevel.LOADFLOW) >= 0));

        if (parent.hasRatioTapChanger()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", parent);
        }

        parent.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
