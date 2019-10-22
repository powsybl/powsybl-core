/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.Leg1Adder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class Leg1AdderAdapter extends AbstractAdapter<Leg1Adder> implements Leg1Adder {

    protected Leg1AdderAdapter(final Leg1Adder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public Leg1AdderAdapter setVoltageLevel(final String voltageLevelId) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setNode(final int node) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setBus(final String bus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setConnectableBus(final String connectableBus) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setRatedU(final double ratedU) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
