/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.RatioTapChangerStepsReplacer;

/**
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public class RatioTapChangerStepsReplacerImpl extends AbstractTapChangerStepsReplacer<RatioTapChangerStepsReplacerImpl, RatioTapChangerStepImpl> implements RatioTapChangerStepsReplacer {

    private Network network;

    class StepAdderImpl implements RatioTapChangerStepsReplacer.StepAdder {

        private final Network network;

        private double rho = Double.NaN;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        public StepAdderImpl(Network network) {
            this.network = network;
        }

        @Override
        public RatioTapChangerStepsReplacer.StepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer.StepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer.StepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer.StepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer.StepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public RatioTapChangerStepsReplacer endStep() {
            RatioTapChangerStepImpl step = new RatioTapChangerStepImpl(network, steps.size(), rho, r, x, g, b);
            steps.add(step);
            return RatioTapChangerStepsReplacerImpl.this;
        }

    }

    RatioTapChangerStepsReplacerImpl(RatioTapChangerImpl ratioTapChanger, Network network) {
        super(ratioTapChanger);
        this.network = network;
    }

    @Override
    public RatioTapChangerStepsReplacer.StepAdder beginStep() {
        return new StepAdderImpl(network);
    }
}
