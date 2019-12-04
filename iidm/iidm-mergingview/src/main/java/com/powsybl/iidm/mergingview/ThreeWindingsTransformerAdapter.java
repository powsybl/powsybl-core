/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapter extends AbstractIdentifiableAdapter<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    ThreeWindingsTransformerAdapter(final ThreeWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Terminal getTerminal(final Side side) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Side getSide(final Terminal terminal) {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Substation getSubstation() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getRatedU0() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg1() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg2() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg3() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
