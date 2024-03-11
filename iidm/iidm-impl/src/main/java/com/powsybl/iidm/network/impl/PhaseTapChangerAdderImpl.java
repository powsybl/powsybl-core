/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.report.TypedValue;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
    public PhaseTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public PhaseTapChanger add() {
        NetworkImpl network = getNetwork();
        if (tapPosition == null) {
            ValidationUtil.throwExceptionOrLogError(parent, "tap position is not set", network.getMinValidationLevel());
            network.setValidationLevelIfGreaterThan(ValidationLevel.EQUIPMENT);
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
                network.setValidationLevelIfGreaterThan(ValidationLevel.EQUIPMENT);
            }
        }
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkPhaseTapChangerRegulation(parent, regulationMode, regulationValue, regulating,
                regulationTerminal, network, network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(parent, "phase tap changer", regulating,
                targetDeadband, network.getMinValidationLevel()));
        PhaseTapChangerImpl tapChanger
                = new PhaseTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, tapPosition, regulating, regulationMode, regulationValue, targetDeadband);

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getPhaseTapChanger());
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers,
                regulating, network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));

        if (parent.hasRatioTapChanger()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", parent);
            network.getReportNodeContext().getReportNode().newReportNode()
                    .withMessageTemplate("validationWarning", "${parent} has both Ratio and Phase Tap Changer.")
                    .withUntypedValue("parent", parent.getMessageHeader())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
        }

        parent.setPhaseTapChanger(tapChanger);
        return tapChanger;
    }

}
