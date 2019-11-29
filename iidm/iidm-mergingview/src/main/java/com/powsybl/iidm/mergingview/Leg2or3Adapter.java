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
public class Leg2or3Adapter extends AbstractAdapter<ThreeWindingsTransformer.Leg2or3> implements ThreeWindingsTransformer.Leg2or3 {

    Leg2or3Adapter(final ThreeWindingsTransformer.Leg2or3 delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Terminal getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getR() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg2or3 setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getX() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg2or3 setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg2or3 setRatedU(final double ratedU) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChangerAdder newRatioTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public RatioTapChanger getRatioTapChanger() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
