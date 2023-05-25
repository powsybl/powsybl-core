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

    private final DanglingLine danglingLine1;
    private final DanglingLine danglingLine2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.danglingLine1 = index.getDanglingLine(delegate.getDanglingLine1());
        this.danglingLine2 = index.getDanglingLine(delegate.getDanglingLine2());
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
    public DanglingLine getDanglingLine1() {
        return danglingLine1;
    }

    @Override
    public DanglingLine getDanglingLine2() {
        return danglingLine2;
    }

    @Override
    public DanglingLine getDanglingLine(Branch.Side side) {
        switch (side) {
            case ONE:
                return danglingLine1;
            case TWO:
                return danglingLine2;
            default:
                throw new IllegalStateException("Unexpected side: " + side);
        }
    }

    @Override
    public DanglingLine getDanglingLine(String voltageLevelId) {

        if (danglingLine1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return danglingLine1;
        }
        if (danglingLine2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return danglingLine2;
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
