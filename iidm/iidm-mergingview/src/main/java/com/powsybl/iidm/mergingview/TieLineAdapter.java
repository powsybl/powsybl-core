/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends LineAdapter implements TieLine {

    class TieLineHalfLineAdapter implements HalfLine {

        private final HalfLine delegate;
        private final MergingViewIndex index;

        TieLineHalfLineAdapter(HalfLine delegate, MergingViewIndex index) {
            this.delegate = Objects.requireNonNull(delegate);
            this.index = Objects.requireNonNull(index);
        }

        @Override
        public double getR() {
            return delegate.getR();
        }

        @Override
        public HalfLine setR(double r) {
            delegate.setR(r);
            return this;
        }

        @Override
        public double getX() {
            return delegate.getX();
        }

        @Override
        public HalfLine setX(double x) {
            delegate.setX(x);
            return this;
        }

        @Override
        public double getG1() {
            return delegate.getG1();
        }

        @Override
        public HalfLine setG1(double g1) {
            delegate.setG1(g1);
            return this;
        }

        @Override
        public double getG2() {
            return delegate.getG2();
        }

        @Override
        public HalfLine setG2(double g2) {
            delegate.setG2(g2);
            return this;
        }

        @Override
        public double getB1() {
            return delegate.getB1();
        }

        @Override
        public HalfLine setB1(double b1) {
            delegate.setB1(b1);
            return this;
        }

        @Override
        public double getB2() {
            return delegate.getB2();
        }

        @Override
        public HalfLine setB2(double b2) {
            delegate.setB2(b2);
            return this;
        }

        @Override
        public String getId() {
            return delegate.getId();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Boundary getBoundary() {
            return new BoundaryAdapter(false, delegate.getBoundary(), index);
        }

        @Override
        public boolean isFictitious() {
            return delegate.isFictitious();
        }

        @Override
        public HalfLine setFictitious(boolean fictitious) {
            delegate.setFictitious(fictitious);
            return this;
        }
    }

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
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
        return new TieLineHalfLineAdapter(((TieLine) getDelegate()).getHalf1(), getIndex());
    }

    @Override
    public HalfLine getHalf2() {
        return new TieLineHalfLineAdapter(((TieLine) getDelegate()).getHalf2(), getIndex());
    }

    @Override
    public HalfLine getHalf(Side side) {
        return ((TieLine) getDelegate()).getHalf(side);
    }
}
