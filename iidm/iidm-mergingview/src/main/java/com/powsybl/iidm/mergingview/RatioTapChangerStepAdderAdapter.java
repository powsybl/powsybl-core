/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.RatioTapChangerAdder;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class RatioTapChangerStepAdderAdapter extends AbstractAdapter<RatioTapChangerAdder.StepAdder> implements RatioTapChangerAdder.StepAdder {

    private final RatioTapChangerAdder parentDelegate;

    RatioTapChangerStepAdderAdapter(final RatioTapChangerAdder parentDelegate, final RatioTapChangerAdder.StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = Objects.requireNonNull(parentDelegate, "RatioTapChangerAdder parent is null");
    }

    @Override
    public RatioTapChangerAdder endStep() {
        getDelegate().endStep();
        return this.parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public RatioTapChangerAdder.StepAdder setRho(final double rho) {
        getDelegate().setRho(rho);
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public RatioTapChangerAdder.StepAdder setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
