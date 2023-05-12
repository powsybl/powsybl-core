/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TieLine;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class TieLineAdapter extends AbstractIdentifiableAdapter<TieLine> implements TieLine {

    private final BoundaryLine boundaryLine1;
    private final BoundaryLine boundaryLine2;

    TieLineAdapter(final TieLine delegate, final MergingViewIndex index) {
        super(delegate, index);
        this.boundaryLine1 = index.getDanglingLine(delegate.getBoundaryLine1());
        this.boundaryLine2 = index.getDanglingLine(delegate.getBoundaryLine2());
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
    public BoundaryLine getBoundaryLine1() {
        return boundaryLine1;
    }

    @Override
    public BoundaryLine getBoundaryLine2() {
        return boundaryLine2;
    }

    @Override
    public BoundaryLine getDanglingLine(Branch.Side side) {
        switch (side) {
            case ONE:
                return boundaryLine1;
            case TWO:
                return boundaryLine2;
            default:
                throw new IllegalStateException("Unexpected side: " + side);
        }
    }

    @Override
    public BoundaryLine getDanglingLine(String voltageLevelId) {

        if (boundaryLine1.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine1;
        }
        if (boundaryLine2.getTerminal().getVoltageLevel().getId().equals(voltageLevelId)) {
            return boundaryLine2;
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
