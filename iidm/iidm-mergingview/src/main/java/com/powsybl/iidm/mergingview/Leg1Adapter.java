/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.CurrentLimits;
import com.powsybl.iidm.network.CurrentLimitsAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg1;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class Leg1Adapter extends AbstractAdapter<ThreeWindingsTransformer.Leg1> implements ThreeWindingsTransformer.Leg1 {

    protected Leg1Adapter(final Leg1 delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public Leg1Adapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public Leg1Adapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getRatedU() {
        return getDelegate().getRatedU();
    }

    @Override
    public Leg1Adapter setRatedU(final double ratedU) {
        getDelegate().setRatedU(ratedU);
        return this;
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return getDelegate().getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return getDelegate().newCurrentLimits();
    }

    @Override
    public double getG() {
        return getDelegate().getG();
    }

    @Override
    public Leg1Adapter setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public Leg1Adapter setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

}
