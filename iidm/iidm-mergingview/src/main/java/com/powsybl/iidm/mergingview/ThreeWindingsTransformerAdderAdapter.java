/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ThreeWindingsTransformerAdderAdapter extends AbstractIdentifiableAdderAdapter<ThreeWindingsTransformerAdder> implements ThreeWindingsTransformerAdder {

    private class LegAdderAdapter extends AbstractAdapter<ThreeWindingsTransformerAdder.LegAdder> implements ThreeWindingsTransformerAdder.LegAdder {

        LegAdderAdapter(final ThreeWindingsTransformerAdder.LegAdder delegate, final MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public ThreeWindingsTransformerAdder add() {
            getDelegate().add();
            return ThreeWindingsTransformerAdderAdapter.this;
        }

        // -------------------------------
        // Simple delegated methods ------
        // -------------------------------
        @Override
        public ThreeWindingsTransformerAdder.LegAdder setVoltageLevel(final String voltageLevelId) {
            getDelegate().setVoltageLevel(voltageLevelId);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setNode(final int node) {
            getDelegate().setNode(node);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setBus(final String bus) {
            getDelegate().setBus(bus);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setConnectableBus(final String connectableBus) {
            getDelegate().setConnectableBus(connectableBus);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setR(final double r) {
            getDelegate().setR(r);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setX(final double x) {
            getDelegate().setX(x);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setRatedU(final double ratedU) {
            getDelegate().setRatedU(ratedU);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setG(final double g) {
            getDelegate().setG(g);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setB(final double b) {
            getDelegate().setB(b);
            return this;
        }

        @Override
        public ThreeWindingsTransformerAdder.LegAdder setRatedS(double ratedS) {
            getDelegate().setRatedS(ratedS);
            return this;
        }
    }

    ThreeWindingsTransformerAdderAdapter(final ThreeWindingsTransformerAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public ThreeWindingsTransformer add() {
        checkAndSetUniqueId();
        return getIndex().getThreeWindingsTransformer(getDelegate().add());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg1() {
        return new LegAdderAdapter(getDelegate().newLeg1(), getIndex());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg2() {
        return new LegAdderAdapter(getDelegate().newLeg2(), getIndex());
    }

    @Override
    public ThreeWindingsTransformerAdder.LegAdder newLeg3() {
        return new LegAdderAdapter(getDelegate().newLeg3(), getIndex());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ThreeWindingsTransformerAdder setRatedU0(double ratedU0) {
        getDelegate().setRatedU0(ratedU0);
        return this;
    }
}
