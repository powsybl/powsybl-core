/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Line;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class LineAdapter extends AbstractConnectableBranchAdapter<Line> implements Line {

    LineAdapter(final Line delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public Line setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public Line setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getG1() {
        return getDelegate().getG1();
    }

    @Override
    public Line setG1(final double g1) {
        getDelegate().setG1(g1);
        return this;
    }

    @Override
    public double getG2() {
        return getDelegate().getG2();
    }

    @Override
    public Line setG2(final double g2) {
        getDelegate().setG2(g2);
        return this;
    }

    @Override
    public double getB1() {
        return getDelegate().getB1();
    }

    @Override
    public Line setB1(final double b1) {
        getDelegate().setB1(b1);
        return this;
    }

    @Override
    public double getB2() {
        return getDelegate().getB2();
    }

    @Override
    public Line setB2(final double b2) {
        getDelegate().setB2(b2);
        return this;
    }
}
