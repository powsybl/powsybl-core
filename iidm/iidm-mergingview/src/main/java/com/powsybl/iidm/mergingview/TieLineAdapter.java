/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends AbstractIdentifiableAdapter<TieLine> implements TieLine {

    private final DanglingLine half1;
    private final DanglingLine half2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.half1 = index.getDanglingLine(delegate.getHalf1());
        this.half2 = index.getDanglingLine(delegate.getHalf2());
    }

    @Override
    public final void remove() {
        throw MergingView.createNotImplementedException();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getUcteXnodeCode() {
        return (getDelegate()).getUcteXnodeCode();
    }

    @Override
    public DanglingLine getHalf1() {
        return half1;
    }

    @Override
    public DanglingLine getHalf2() {
        return half2;
    }

    @Override
    public DanglingLine getHalf(Branch.Side side) {
        switch (side) {
            case ONE:
                return half1;
            case TWO:
                return half2;
            default:
                throw new AssertionError("Unexpected side: " + side);
        }
    }

    @Override
    public DanglingLine getHalf(String voltageLevelId) {

        if (half1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half1;
        }
        if (half2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return half2;
        }
        return null;
    }

    @Override
    public double getR() {
        return getDelegate().getR();
    }

    @Override
    public double getX() {
        return getDelegate().getX();
    }

    @Override
    public double getG1() {
        return getDelegate().getG1();
    }

    @Override
    public double getG2() {
        return getDelegate().getG2();
    }

    @Override
    public double getB1() {
        return getDelegate().getB1();
    }

    @Override
    public double getB2() {
        return getDelegate().getB2();
    }
}
