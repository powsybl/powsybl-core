/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.Leg2or3Adder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class Leg2or3AdderAdapter extends AbstractAdapter<Leg2or3Adder> implements Leg2or3Adder {

    private final ThreeWindingsTransformerAdderAdapter parentDelegate;

    protected Leg2or3AdderAdapter(final ThreeWindingsTransformerAdderAdapter parentDelegate, final Leg2or3Adder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = parentDelegate;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter add() {
        getDelegate().add();
        return this.parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public Leg2or3AdderAdapter setVoltageLevel(final String voltageLevelId) {
        getDelegate().setVoltageLevel(voltageLevelId);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public Leg2or3AdderAdapter setRatedU(final double ratedU) {
        getDelegate().setRatedU(ratedU);
        return this;
    }
}
