/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.TwoWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdderAdapter extends AbstractAdapter<TwoWindingsTransformerAdder> implements TwoWindingsTransformerAdder {

    protected TwoWindingsTransformerAdderAdapter(final TwoWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TwoWindingsTransformerAdderAdapter setVoltageLevel1(final String voltageLevelId1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setNode1(final int node1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setBus1(final String bus1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setConnectableBus1(final String connectableBus1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setVoltageLevel2(final String voltageLevelId2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setNode2(final int node2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setBus2(final String bus2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setConnectableBus2(final String connectableBus2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setR(final double r) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setX(final double x) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setB(final double b) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setG(final double g) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setRatedU1(final double ratedU1) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdderAdapter setRatedU2(final double ratedU2) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public TwoWindingsTransformerAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
