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

    private final RatioTapChangerAdderAdapter parentDelegate;

    protected RatioTapChangerStepAdderAdapter(final RatioTapChangerAdderAdapter parentDelegate, final StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = parentDelegate;
    }

    @Override
    public RatioTapChangerAdderAdapter endStep() {
        getDelegate().endStep();
        return this.parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public RatioTapChangerStepAdderAdapter setRho(final double rho) {
        getDelegate().setRho(rho);
        return this;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public RatioTapChangerStepAdderAdapter setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
