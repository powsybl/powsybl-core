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
class RatioTapChangerAdderImpl implements RatioTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatioTapChangerAdderImpl.class);

    private final RatioTapChangerParent parent;

    private int lowTapPosition = 0;

    private Integer tapPosition;

    private final List<RatioTapChangerStepImpl> steps = new ArrayList<>();

    private boolean loadTapChangingCapabilities = false;

    private boolean regulating = false;

    private double targetV = Double.NaN;

    private double targetDeadband = Double.NaN;

    private TerminalExt regulationTerminal;

    class StepAdderImpl implements StepAdder {

        private double rho = Double.NaN;

        private double r = Double.NaN;

        private double x = Double.NaN;

        private double g = Double.NaN;

        private double b = Double.NaN;

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
            if (Double.isNaN(g)) {
                throw new ValidationException(parent, "step g is not set");
            }
            if (Double.isNaN(b)) {
                throw new ValidationException(parent, "step b is not set");
            }
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(steps.size(), rho, r, x, g, b);
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
    public StepAdder beginStep() {
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
                targetV, network, network.getMinValidationLevel()));
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkTargetDeadband(parent, "ratio tap changer", regulating, targetDeadband,
                network.getMinValidationLevel()));
        RatioTapChangerImpl tapChanger
                = new RatioTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities,
                                          tapPosition, regulating, targetV, targetDeadband);

        Set<TapChanger<?, ?>> tapChangers = new HashSet<>(parent.getAllTapChangers());
        tapChangers.remove(parent.getRatioTapChanger());
        network.setValidationLevelIfGreaterThan(ValidationUtil.checkOnlyOneTapChangerRegulatingEnabled(parent, tapChangers, regulating,
                network.getMinValidationLevel().compareTo(ValidationLevel.STEADY_STATE_HYPOTHESIS) >= 0));

        if (parent.hasPhaseTapChanger()) {
            LOGGER.warn("{} has both Ratio and Phase Tap Changer", parent);
        }

        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

}
