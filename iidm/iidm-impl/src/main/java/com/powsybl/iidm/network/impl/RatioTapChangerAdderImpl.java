/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
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
class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatioTapChangerAdderImpl.class);

    private final RatioTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<RatioTapChangerStepImpl> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private boolean regulating = false;

    private RatioTapChanger.RegulationMode regulationMode = null;

    private double regulationValue = Double.NaN;

    private double targetDeadband = Double.NaN;

    private TerminalExt regulationTerminal;

    class StepAdderImpl implements RatioTapChangerAdder.StepAdder {

        private double rho = Double.NaN;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        @Override
        public RatioTapChangerAdder.StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public RatioTapChangerAdder.StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public RatioTapChangerAdder endStep() {
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(steps.size(), rho, r, x, g, b);
            step.validate(parent);
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
        if (!Double.isNaN(targetV)) {
            this.regulationMode = RatioTapChanger.RegulationMode.VOLTAGE;
        }
        this.regulationValue = targetV;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationMode(RatioTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    public RatioTapChanger add() {
        NetworkImpl network = getNetwork();
        if (tapPosition == null) {
            ValidationUtil.throwExceptionOrLogError(parent, "tap position is not set", network.getMinValidationLevel());
            network.setValidationLevelIfGreaterThan(ValidationLevel.EQUIPMENT);
        }
        if (steps.isEmpty()) {
            throw new ValidationException(parent, "ratio tap changer should have at least one step");
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
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, loadTapChangingCapabilities, regulationTerminal,
                regulationMode, regulationValue, network, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(parent, "ratio tap changer", regulating, targetDeadband,
                network.getMinValidationLevel()));
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities,
                                          tapPosition, regulating, regulationMode, regulationValue, targetDeadband);

        Set<TapChanger<?, ?, ?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
                network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));

        if (parent.hasPhaseTapChanger()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", parent);
            network.getReportNodeContext().getReportNode().newReportNode()
                    .withMessageTemplate("validationWarning", "${parent} has both Ratio and Phase Tap Changer.")
                    .withUntypedValue("parent", parent.getMessageHeader())
                    .withSeverity(TypedValue.WARN_SEVERITY)
                    .add();
        }

        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
