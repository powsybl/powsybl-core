/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.RatioTapChanger;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.ValidationLevel;
import com.powsybl.iidm.network.ValidationUtil;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class RatioTapChangerAdderImpl extends AbstractTapChangerAdderImpl<RatioTapChangerAdderImpl, RatioTapChangerParent, RatioTapChanger, RatioTapChangerStepImpl> implements RatioTapChangerAdder {

    private RatioTapChanger.RegulationMode regulationMode = null;

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
        super(parent, false);
    }

    @Override
    public RatioTapChangerAdder setTargetV(double targetV) {
        if (!Double.isNaN(targetV)) {
            this.regulationMode = RatioTapChanger.RegulationMode.VOLTAGE;
        }
        return setRegulationValue(targetV);
    }

    @Override
    public RatioTapChangerAdder setRegulationMode(RatioTapChanger.RegulationMode regulationMode) {
        this.regulationMode = regulationMode;
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder beginStep() {
        return new StepAdderImpl();
    }

    @Override
    protected RatioTapChanger createTapChanger(RatioTapChangerParent parent, int lowTapPosition, List<RatioTapChangerStepImpl> steps, TerminalExt regulationTerminal, Integer tapPosition, boolean regulating, boolean loadTapChangingCapabilities, double regulationValue, double targetDeadband) {
        RatioTapChangerImpl tapChanger = new RatioTapChangerImpl(parent, lowTapPosition, steps, regulationTerminal, loadTapChangingCapabilities,
                tapPosition, regulating, regulationMode, regulationValue, targetDeadband);
        parent.setRatioTapChanger(tapChanger);
        return tapChanger;
    }

    @Override
    protected RatioTapChangerAdderImpl self() {
        return this;
    }

    @Override
    protected ValidationLevel checkTapChangerRegulation(RatioTapChangerParent parent, double regulationValue, boolean regulating, boolean loadTapChangingCapabilities, TerminalExt regulationTerminal) {
        return ValidationUtil.checkRatioTapChangerRegulation(parent, regulating, loadTapChangingCapabilities, regulationTerminal,
                regulationMode, regulationValue, getNetwork(), getNetwork().getMinValidationLevel(), getNetwork().getReportNodeContext().getReportNode());
    }

    @Override
    protected String getValidableType() {
        return "ratio tap changer";
    }
}
