/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChangerAdder;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class PhaseTapChangerStepAdderAdapter extends AbstractAdapter<PhaseTapChangerAdder.StepAdder> implements PhaseTapChangerAdder.StepAdder {

    private final PhaseTapChangerAdder parentDelegate;

    PhaseTapChangerStepAdderAdapter(final PhaseTapChangerAdder parentDelegate, final PhaseTapChangerAdder.StepAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = Objects.requireNonNull(parentDelegate, "PhaseTapChangerAdder parent is null");
    }

    @Override
    public PhaseTapChangerAdder endStep() {
        getDelegate().endStep();
        return parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public PhaseTapChangerAdder.StepAdder setAlpha(final double alpha) {
        getDelegate().setAlpha(alpha);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder setRho(final double rho) {
        getDelegate().setRho(rho);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public PhaseTapChangerAdder.StepAdder setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
