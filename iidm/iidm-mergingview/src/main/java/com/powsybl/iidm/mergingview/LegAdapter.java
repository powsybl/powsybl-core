/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LegAdapter extends AbstractAdapter<ThreeWindingsTransformer.Leg> implements ThreeWindingsTransformer.Leg {

    LegAdapter(final ThreeWindingsTransformer.Leg delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Terminal getTerminal() {
        return getIndex().getTerminal(getDelegate().getTerminal());
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderAdapter(getDelegate().newPhaseTapChanger(), getIndex());
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderAdapter(getDelegate().newRatioTapChanger(), getIndex());
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
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
    public ThreeWindingsTransformer.Leg setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public ThreeWindingsTransformer.Leg setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getRatedU() {
        return getDelegate().getRatedU();
    }

    @Override
    public ThreeWindingsTransformer.Leg setRatedU(final double ratedU) {
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
    public ThreeWindingsTransformer.Leg setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public ThreeWindingsTransformer.Leg setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
