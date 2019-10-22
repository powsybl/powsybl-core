/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdderAdapter extends AbstractAdapter<ThreeWindingsTransformerAdder> implements ThreeWindingsTransformerAdder {

    protected ThreeWindingsTransformerAdderAdapter(final ThreeWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public ThreeWindingsTransformerAdderAdapter setId(final String id) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter setName(final String name) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg1AdderAdapter newLeg1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg2or3AdderAdapter newLeg2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Leg2or3AdderAdapter newLeg3() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformerAdapter add() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
