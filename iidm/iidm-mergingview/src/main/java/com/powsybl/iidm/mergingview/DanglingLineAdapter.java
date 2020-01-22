/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapter extends AbstractInjectionAdapter<DanglingLine> implements DanglingLine {

    DanglingLineAdapter(final DanglingLine delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public double getP0() {
        return getDelegate().getP0();
    }

    @Override
    public DanglingLine setP0(final double p0) {
        getDelegate().setP0(p0);
        return this;
    }

    @Override
    public double getQ0() {
        return getDelegate().getQ0();
    }

    @Override
    public DanglingLine setQ0(final double q0) {
        getDelegate().setQ0(q0);
        return this;
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public DanglingLine setR(final double r) {
        getDelegate().setR(r);
        return this;
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public DanglingLine setX(final double x) {
        getDelegate().setX(x);
        return this;
    }

    @Override
    public double getG() {
        return getDelegate().getG();
    }

    @Override
    public DanglingLine setG(final double g) {
        getDelegate().setG(g);
        return this;
    }

    @Override
    public double getB() {
        return getDelegate().getB();
    }

    @Override
    public DanglingLine setB(final double b) {
        getDelegate().setB(b);
        return this;
    }

    @Override
    public String getUcteXnodeCode() {
        return getDelegate().getUcteXnodeCode();
    }

    @Override
    public CurrentLimits getCurrentLimits() {
        return getDelegate().getCurrentLimits();
    }

    @Override
    public CurrentLimitsAdder newCurrentLimits() {
        return getDelegate().newCurrentLimits();
    }
}
