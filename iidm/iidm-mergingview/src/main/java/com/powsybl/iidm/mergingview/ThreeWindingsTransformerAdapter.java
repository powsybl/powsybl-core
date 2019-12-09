/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This adaptation hide true implementation of {@link ThreeWindingsTransformer}.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapter extends AbstractIdentifiableAdapter<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private LegAdapter leg1;

    private LegAdapter leg2;

    private LegAdapter leg3;

    ThreeWindingsTransformerAdapter(final ThreeWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
        // no need to store LegAdapter in MergingViewIndex
        leg1 = new LegAdapter(getDelegate().getLeg1(), getIndex());
        leg2 = new LegAdapter(getDelegate().getLeg2(), getIndex());
        leg3 = new LegAdapter(getDelegate().getLeg3(), getIndex());
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg1() {
        return leg1;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg2() {
        return leg2;
    }

    @Override
    public ThreeWindingsTransformer.Leg getLeg3() {
        return leg3;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
                                           .map(getIndex()::getTerminal)
                                           .collect(Collectors.toList());
    }

    @Override
    public Terminal getTerminal(final Side side) {
        return getIndex().getTerminal(getDelegate().getTerminal(side));
    }

    @Override
    public Side getSide(final Terminal side) {
        Terminal terminal = side;
        if (terminal instanceof TerminalAdapter) {
            terminal = ((TerminalAdapter) terminal).getDelegate();
        }
        return getDelegate().getSide(terminal);
    }

    @Override
    public Substation getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public double getRatedU0() {
        return getDelegate().getRatedU0();
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
