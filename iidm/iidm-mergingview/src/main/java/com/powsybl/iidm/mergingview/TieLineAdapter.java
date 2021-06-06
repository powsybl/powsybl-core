/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends LineAdapter implements TieLine {

    class TieLineHalfLineAdapter extends AbstractAdapter<HalfLine> implements HalfLine {

        TieLineHalfLineAdapter(HalfLine delegate, MergingViewIndex index) {
            super(delegate, index);
        }

        @Override
        public double getR() {
            return getDelegate().getR();
        }

        @Override
        public HalfLine setR(double r) {
            getDelegate().setR(r);
            return this;
        }

        @Override
        public double getX() {
            return getDelegate().getX();
        }

        @Override
        public HalfLine setX(double x) {
            getDelegate().setX(x);
            return this;
        }

        @Override
        public double getG1() {
            return getDelegate().getG1();
        }

        @Override
        public HalfLine setG1(double g1) {
            getDelegate().setG1(g1);
            return this;
        }

        @Override
        public double getG2() {
            return getDelegate().getG2();
        }

        @Override
        public HalfLine setG2(double g2) {
            getDelegate().setG2(g2);
            return this;
        }

        @Override
        public double getB1() {
            return getDelegate().getB1();
        }

        @Override
        public HalfLine setB1(double b1) {
            getDelegate().setB1(b1);
            return this;
        }

        @Override
        public double getB2() {
            return getDelegate().getB2();
        }

        @Override
        public HalfLine setB2(double b2) {
            getDelegate().setB2(b2);
            return this;
        }

        @Override
        public String getId() {
            return getDelegate().getId();
        }

        @Override
        public String getName() {
            return getDelegate().getName();
        }

        @Override
        public Boundary getBoundary() {
            return getIndex().getBoundary(getDelegate().getBoundary());
        }

        @Override
        public boolean isFictitious() {
            return getDelegate().isFictitious();
        }

        @Override
        public HalfLine setFictitious(boolean fictitious) {
            getDelegate().setFictitious(fictitious);
            return this;
        }
    }

    private final HalfLine half1;
    private final HalfLine half2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.half1 = new TieLineHalfLineAdapter(delegate.getHalf1(), index);
        this.half2 = new TieLineHalfLineAdapter(delegate.getHalf2(), index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getUcteXnodeCode() {
        return ((TieLine) getDelegate()).getUcteXnodeCode();
    }

    @Override
    public HalfLine getHalf1() {
        return half1;
    }

    @Override
    public HalfLine getHalf2() {
        return half2;
    }

    @Override
    public HalfLine getHalf(Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }
}
