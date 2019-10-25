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

    private final PhaseTapChangerAdderAdapter parentDelegate;

    protected PhaseTapChangerStepAdderAdapter(final PhaseTapChangerAdderAdapter parentDelegate, final StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = parentDelegate;
    }

    @Override
    public PhaseTapChangerAdderAdapter endStep() {
        getDelegate().endStep();
        return parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public PhaseTapChangerStepAdderAdapter setAlpha(final double alpha) {
        getDelegate().setAlpha(alpha);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setRho(final double rho) {
        getDelegate().setRho(rho);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public PhaseTapChangerStepAdderAdapter setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
