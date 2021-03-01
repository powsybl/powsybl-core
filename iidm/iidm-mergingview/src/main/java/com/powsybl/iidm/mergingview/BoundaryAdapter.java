/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class BoundaryAdapter extends AbstractAdapter<Boundary> implements Boundary {

    private final Branch.Side mergedSide;

    BoundaryAdapter(Boundary delegate, MergingViewIndex index) {
        this(delegate, null, index);
    }

    BoundaryAdapter(Boundary delegate, Branch.Side mergedSide, MergingViewIndex index) {
        super(delegate, index);
        this.mergedSide = mergedSide;
    }

    @Override
    public double getV() {
        return getDelegate().getV();
    }

    @Override
    public double getAngle() {
        return getDelegate().getAngle();
    }

    @Override
    public double getP() {
        return getDelegate().getP();
    }

    @Override
    public double getQ() {
        return getDelegate().getQ();
    }

    @Override
    public Branch.Side getSide() {
        return getDelegate().getSide() == null ? mergedSide : getDelegate().getSide();
    }

    @Override
    public Connectable getConnectable() {
        return getIndex().getConnectable(getDelegate().getConnectable());
    }
}
