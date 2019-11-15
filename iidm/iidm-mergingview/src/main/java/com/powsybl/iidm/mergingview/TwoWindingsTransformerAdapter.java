/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdapter extends AbstractBranchAdapter<TwoWindingsTransformer> implements TwoWindingsTransformer {

    protected TwoWindingsTransformerAdapter(final TwoWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public PhaseTapChangerAdapter getPhaseTapChanger() {
        return getIndex().getPhaseTapChanger(getDelegate().getPhaseTapChanger());
    }

    @Override
    public RatioTapChangerAdapter getRatioTapChanger() {
        return getIndex().getRatioTapChanger(getDelegate().getRatioTapChanger());
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public PhaseTapChangerAdder newPhaseTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public SubstationAdapter getSubstation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU1(final double ratedU1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter setRatedU2(final double ratedU2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
