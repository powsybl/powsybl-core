/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TwoWindingsTransformerAdderAdapter extends AbstractIdentifiableAdderAdapter<TwoWindingsTransformer, TwoWindingsTransformerAdder> implements TwoWindingsTransformerAdder {

    TwoWindingsTransformerAdderAdapter(final TwoWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public TwoWindingsTransformer add() {
        checkAndSetUniqueId();
        return getIndex().getTwoWindingsTransformer(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public TwoWindingsTransformerAdder setVoltageLevel1(final String voltageLevelId1) {
        getDelegate().setVoltageLevel1(voltageLevelId1);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setNode1(final int node1) {
        getDelegate().setNode1(node1);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setBus1(final String bus1) {
        getDelegate().setBus1(bus1);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setConnectableBus1(final String connectableBus1) {
        getDelegate().setConnectableBus1(connectableBus1);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setVoltageLevel2(final String voltageLevelId2) {
        getDelegate().setVoltageLevel2(voltageLevelId2);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setNode2(final int node2) {
        getDelegate().setNode2(node2);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setBus2(final String bus2) {
        getDelegate().setBus2(bus2);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setConnectableBus2(final String connectableBus2) {
        getDelegate().setConnectableBus2(connectableBus2);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU1(final double ratedU1) {
        getDelegate().setRatedU1(ratedU1);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedU2(final double ratedU2) {
        getDelegate().setRatedU2(ratedU2);
        return this;
    }

    @Override
    public TwoWindingsTransformerAdder setRatedS(double ratedS) {
        getDelegate().setRatedS(ratedS);
        return this;
    }
}
