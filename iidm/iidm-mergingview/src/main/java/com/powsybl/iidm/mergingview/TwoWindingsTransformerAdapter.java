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
    public Substation getSubstation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getG() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getB() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setRatedU1(final double ratedU1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformer setRatedU2(final double ratedU2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
