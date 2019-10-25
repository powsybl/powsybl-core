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

    private final ThreeWindingsTransformerAdderAdapter parentDelegate;

    protected Leg1AdderAdapter(final ThreeWindingsTransformerAdderAdapter parentDelegate, final Leg1Adder delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.parentDelegate = parentDelegate;
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter add() {
        getDelegate().add();
        return parentDelegate;
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public Leg1AdderAdapter setVoltageLevel(final String voltageLevelId) {
        getDelegate().setVoltageLevel(voltageLevelId);
        return this;
    }

    @Override
    public Leg1AdderAdapter setNode(final int node) {
        getDelegate().setNode(node);
        return this;
    }

    @Override
    public Leg1AdderAdapter setBus(final String bus) {
        getDelegate().setBus(bus);
        return this;
    }

    @Override
    public Leg1AdderAdapter setConnectableBus(final String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return this;
    }

    @Override
    public Leg1AdderAdapter setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public Leg1AdderAdapter setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public Leg1AdderAdapter setRatedU(final double ratedU) {
        getDelegate().setRatedU(ratedU);
        return this;
    }

    @Override
    public Leg1AdderAdapter setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public Leg1AdderAdapter setB(final double b) {
        getDelegate().setB(b);
        return this;
    }
}
