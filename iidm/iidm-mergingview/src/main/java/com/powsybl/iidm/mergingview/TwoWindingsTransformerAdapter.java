/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Optional;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapter extends AbstractBranchAdapter<TwoWindingsTransformer> implements TwoWindingsTransformer {

    TwoWindingsTransformerAdapter(final TwoWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChanger getPhaseTapChanger() {
        return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        return new RatioTapChangerAdderAdapter(getDelegate().newRatioTapChanger(), getIndex());
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        return new PhaseTapChangerAdderAdapter(getDelegate().newPhaseTapChanger(), getIndex());
    }

    @Override
    public Optional<Substation> getSubstation() {
        return getDelegate().getSubstation().map(s -> getIndex().getSubstation(s));
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public TwoWindingsTransformer setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public TwoWindingsTransformer setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getG() {
        return getDelegate().getG();
    }

    @Override
    public TwoWindingsTransformer setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public TwoWindingsTransformer setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public double getRatedU1() {
        return getDelegate().getRatedU1();
    }

    @Override
    public TwoWindingsTransformer setRatedU1(final double ratedU1) {
        getDelegate().setRatedU1(ratedU1);
        return this;
    }

    @Override
    public double getRatedU2() {
        return getDelegate().getRatedU2();
    }

    @Override
    public TwoWindingsTransformer setRatedU2(final double ratedU2) {
        getDelegate().setRatedU2(ratedU2);
        return this;
    }

    @Override
    public double getRatedS() {
        return getDelegate().getRatedS();
    }

    @Override
    public TwoWindingsTransformer setRatedS(double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }
}
