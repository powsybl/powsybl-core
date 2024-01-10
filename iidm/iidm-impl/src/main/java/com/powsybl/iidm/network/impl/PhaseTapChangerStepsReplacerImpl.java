/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChangerStepsReplacer;
import com.powsybl.iidm.network.PhaseTapChangerStepsReplacerStepAdder;

/**
 * @author Florent MILLOT {@literal <florent.millot at rte-france.com>}
 */
public class PhaseTapChangerStepsReplacerImpl extends AbstractTapChangerStepsReplacer<PhaseTapChangerStepsReplacerImpl, PhaseTapChangerStepImpl> implements PhaseTapChangerStepsReplacer {

    class StepAdderImpl implements PhaseTapChangerStepsReplacerStepAdder {

        private double alpha = Double.NaN;

        private double rho = Double.NaN;

        private double r = 0.0;

        private double x = 0.0;

        private double g = 0.0;

        private double b = 0.0;

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setAlpha(double alpha) {
            this.alpha = alpha;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setRho(double rho) {
            this.rho = rho;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setR(double r) {
            this.r = r;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setX(double x) {
            this.x = x;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setG(double g) {
            this.g = g;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacerStepAdder setB(double b) {
            this.b = b;
            return this;
        }

        @Override
        public PhaseTapChangerStepsReplacer endStep() {
            PhaseTapChangerStepImpl step = new PhaseTapChangerStepImpl(steps.size(), alpha, rho, r, x, g, b);
            steps.add(step);
            return PhaseTapChangerStepsReplacerImpl.this;
        }

    }

    PhaseTapChangerStepsReplacerImpl(PhaseTapChangerImpl phaseTapChanger) {
        super(phaseTapChanger);
    }

    @Override
    public PhaseTapChangerStepsReplacerStepAdder beginStep() {
        return new StepAdderImpl();
    }
}
