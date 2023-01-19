/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Boundary;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.VoltageLevel;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class BoundaryAdapter extends AbstractAdapter<Boundary> implements Boundary {

    BoundaryAdapter(Boundary delegate, MergingViewIndex index) {
        super(delegate, index);
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

    /**
     * @deprecated Not used anymore.
     */
    @Deprecated(since = "5.2.0")
    public Branch.Side getSide() {
        throw new PowsyblException("Deprecated. Not used anymore");
    }

    @Override
    public Connectable getConnectable() {
        return getIndex().getConnectable(getDelegate().getConnectable());
    }

    @Override
    public DanglingLine getDanglingLine() {
        return getIndex().getDanglingLine(getDelegate().getDanglingLine());
    }

    @Override
    public VoltageLevel getNetworkSideVoltageLevel() {
        return getIndex().getVoltageLevel(getDelegate().getNetworkSideVoltageLevel());
    }
}
