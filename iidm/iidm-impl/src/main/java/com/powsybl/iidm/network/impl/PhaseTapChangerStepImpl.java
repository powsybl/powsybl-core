/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.PhaseTapChangerStep;
import com.powsybl.iidm.network.ValidationException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseTapChangerStepImpl extends TapChangerStepImpl<PhaseTapChangerStepImpl>
                              implements PhaseTapChangerStep {

    private double alpha;

    PhaseTapChangerStepImpl(int position, double alpha, double rho, double r, double x, double g, double b) {
        super(position, rho, r, x, g, b);
        this.alpha = alpha;
    }

    @Override
    public double getAlpha() {
        return alpha;
    }

    @Override
    public PhaseTapChangerStep setAlpha(double alpha) {
        double oldValue = this.alpha;
        this.alpha = alpha;
        notifyUpdate("alpha", oldValue, alpha);
        return this;
    }

    @Override
    public void validate(TapChangerParent parent) {
        super.validate(parent);
        if (Double.isNaN(this.getAlpha())) {
            throw new ValidationException(parent, "step alpha is not set");
        }
    }
}
