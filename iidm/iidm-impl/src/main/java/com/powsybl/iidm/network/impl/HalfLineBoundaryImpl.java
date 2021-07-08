/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.util.AbstractHalfLineBoundaryImpl;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class HalfLineBoundaryImpl extends AbstractHalfLineBoundaryImpl {

    HalfLineBoundaryImpl(TieLineImpl.HalfLineImpl parent, Branch.Side side) {
        super(parent, side);
    }

    @Override
    public TieLine getConnectable() {
        return ((TieLineImpl.HalfLineImpl) getParent()).getParent();
    }
}
