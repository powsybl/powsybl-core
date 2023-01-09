/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.util.DanglingLineBoundaryImpl;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
class MergedDanglingLineBoundaryImpl extends DanglingLineBoundaryImpl {

    MergedDanglingLineBoundaryImpl(TieLineImpl.MergedDanglingLine parent, Branch.Side side) {
        super(parent, side);
    }

    @Override
    public TieLineImpl getConnectable() {
        return ((TieLineImpl.MergedDanglingLine) parent).parent;
    }
}
