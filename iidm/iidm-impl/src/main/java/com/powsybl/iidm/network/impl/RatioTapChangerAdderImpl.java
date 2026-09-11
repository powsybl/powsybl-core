/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.regulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RatioTapChangerAdderImpl extends AbstractTapChangerAdderImpl<RatioTapChangerAdderImpl, RatioTapChangerParent, RatioTapChanger, RatioTapChangerStepImpl> implements RatioTapChangerAdder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatioTapChangerAdderImpl.class);

    private Boolean regulating = null;
    private double regulationValue = Double.NaN;
    private double targetDeadband = Double.NaN;
    private TerminalExt regulationTerminal;
    private RegulationMode regulationMode = null;
    private VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes = null;

    class StepAdderImpl extends AbstractBasePropertiesHolder implements RatioTapChangerAdder.StepAdder {

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
            this.copyPropertiesTo(step);
            step.validate(parent);
            steps.add(step);
            return RatioTapChangerAdderImpl.this;
        }

    }

    RatioTapChangerAdderImpl(RatioTapChangerParent parent) {
        super(parent, false);
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        if (!Double.isNaN(targetV)) {
            this.regulationMode = RegulationMode.VOLTAGE;
        }
        return setRegulationValue(targetV);
    }

    @Override
    public RatioTapChangerAdder setRegulating(boolean regulating) {
        this.regulating = regulating;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationTerminal(Terminal regulationTerminal) {
        this.regulationTerminal = (TerminalExt) regulationTerminal;
        return this;
    }

    @Override
    public RatioTapChangerAdder setTargetDeadband(double targetDeadband) {
        this.targetDeadband = targetDeadband;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationMode(RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public RatioTapChangerAdder setRegulationValue(double regulationValue) {
        this.regulationValue = regulationValue;
        return this;
    }

    @Override
    public VoltageRegulationAdder<RatioTapChangerAdder> newVoltageRegulation() {
        return new VoltageRegulationAdderImpl<>(RatioTapChanger.class, parent, this, parent.getNetwork().getRef(), this::setVoltageRegulationAttributes);
    }

    @Override
    public double getLocalTargetQ() {
        return Double.NaN;
    }

    @Override
    public RatioTapChangerAdder setLocalTargetQ(double localTargetQ) {
        LOGGER.warn("Ignored operation, the local target reactive power is not supported for ratio tap changer");
        return this;
    }

    @Override
    public RatioTapChangerAdder setLocalTargetV(double localTargetV) {
        LOGGER.warn("Ignored operation, the local target voltage is not supported for ratio tap changer");
        return this;
    }

    private void setVoltageRegulationAttributes(VoltageRegulation.AttributesWithTerminal voltageRegulationAttributes) {
        this.voltageRegulationAttributes = voltageRegulationAttributes;
    }

    @Override
    public RatioTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    protected RatioTapChanger createTapChanger(RatioTapChangerParent parent, int lowTapPosition,
                                               List<RatioTapChangerStepImpl> steps, Integer tapPosition, Integer solvedTapPosition, boolean loadTapChangingCapabilities) {
        // Backward compatibility
        if (voltageRegulationAttributes == null && regulating != null) {
            this.newVoltageRegulation()
                .withMode(regulationMode)
                .withTargetValue(regulationValue)
                .withTerminal(regulationTerminal)
                .withRegulating(regulating)
                .withTargetDeadband(targetDeadband)
                .add();
        }
        NetworkImpl network = getNetwork();

        if (voltageRegulationAttributes != null) {
            network.setValidationLevelIfGreaterThan(ValidationUtil.checkRatioTapChangerRegulation(parent,
                voltageRegulationAttributes,
                loadTapChangingCapabilities,
                getNetwork(),
                getNetwork().getMinValidationLevel(),
                getNetwork().getReportNodeContext().getReportNode()));
        }

        RatioTapChangerImpl tapChanger = new RatioTapChangerImpl(parent, lowTapPosition, steps, loadTapChangingCapabilities,
            tapPosition, solvedTapPosition, voltageRegulationAttributes);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

    @Override
    protected RatioTapChangerAdderImpl self() {
        return this;
    }

    @Override
    protected String getValidableType() {
        return "ratio tap changer";
    }
}
