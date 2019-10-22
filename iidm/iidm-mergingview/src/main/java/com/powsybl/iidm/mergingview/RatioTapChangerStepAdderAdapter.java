/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder.StepAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerStepAdderAdapter extends AbstractAdapter<RatioTapChangerAdder.StepAdder> implements RatioTapChangerAdder.StepAdder {

    protected RatioTapChangerStepAdderAdapter(final StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public RatioTapChangerStepAdderAdapter setRho(final double rho) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerAdderAdapter endStep() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
