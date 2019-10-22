/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.PhaseTapChangerAdder.StepAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerStepAdderAdapter extends AbstractAdapter<PhaseTapChangerAdder.StepAdder> implements PhaseTapChangerAdder.StepAdder {

    protected PhaseTapChangerStepAdderAdapter(final StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public PhaseTapChangerStepAdderAdapter setAlpha(final double alpha) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setRho(final double rho) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdderAdapter endStep() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
