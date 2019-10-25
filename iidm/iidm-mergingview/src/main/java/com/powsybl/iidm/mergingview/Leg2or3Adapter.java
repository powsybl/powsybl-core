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

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class Leg2or3Adapter extends AbstractAdapter<ThreeWindingsTransformer.Leg2or3> implements ThreeWindingsTransformer.Leg2or3 {

    protected Leg2or3Adapter(final ThreeWindingsTransformer.Leg2or3 delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public TerminalAdapter getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public RatioTapChangerAdderAdapter newRatioTapChanger() {
        return new RatioTapChangerAdderAdapter(getDelegate().newRatioTapChanger(), getIndex());
    }

    @Override
    public RatioTapChangerAdapter getRatioTapChanger() {
        return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public Leg2or3Adapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public Leg2or3Adapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getRatedU() {
        return getDelegate().getRatedU();
    }

    @Override
    public Leg2or3Adapter setRatedU(final double ratedU) {
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
}
