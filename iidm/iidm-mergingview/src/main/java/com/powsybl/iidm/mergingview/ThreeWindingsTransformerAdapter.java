/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;

/**
 * This adaptation hide true implementation of {@link ThreeWindingsTransformer}.
 *
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdapter extends AbstractIdentifiableAdapter<ThreeWindingsTransformer> implements ThreeWindingsTransformer {

    private Leg1Adapter adaptedLeg1;

    private Leg2or3Adapter adaptedLeg2;

    private Leg2or3Adapter adaptedLeg3;

    protected ThreeWindingsTransformerAdapter(final ThreeWindingsTransformer delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Leg1Adapter getLeg1() {
        // no need to store Leg1Adapter in MergingViewIndex
        if (adaptedLeg1 == null) {
            adaptedLeg1 = new Leg1Adapter(getDelegate().getLeg1(), getIndex());
        }
        return adaptedLeg1;
    }

    @Override
    public Leg2or3Adapter getLeg2() {
        // no need to store Leg2or3Adapter in MergingViewIndex
        if (adaptedLeg2 == null) {
            adaptedLeg2 = new Leg2or3Adapter(getDelegate().getLeg2(), getIndex());
        }
        return adaptedLeg2;
    }

    @Override
    public Leg2or3Adapter getLeg3() {
        // no need to store Leg2or3Adapter in MergingViewIndex
        if (adaptedLeg3 == null) {
            adaptedLeg3 = new Leg2or3Adapter(getDelegate().getLeg3(), getIndex());
        }
        return adaptedLeg3;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

    @Override
    public TerminalAdapter getTerminal(final Side side) {
        return getIndex().getTerminal(getDelegate().getTerminal(side));
    }

    @Override
    public Side getSide(final Terminal terminal) {
        Terminal param = terminal;
        if (terminal instanceof AbstractAdapter<?>) {
            param = ((AbstractAdapter<Terminal>) terminal).getDelegate();
        }
        return getDelegate().getSide(param);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public SubstationAdapter getSubstation() {
        return getIndex().getSubstation(getDelegate().getSubstation());
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }
}
