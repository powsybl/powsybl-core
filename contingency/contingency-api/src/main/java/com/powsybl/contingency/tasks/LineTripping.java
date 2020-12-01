/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.tasks;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class LineTripping extends BranchTripping {

    public LineTripping(String lineId) {
        this(lineId, null);
    }

    public LineTripping(String lineId, String voltageLevelId) {
        super(lineId, voltageLevelId, Network::getLine);
    }

    @Override
    protected PowsyblException createNotFoundException() {
        return new PowsyblException("Line '" + getBranchId() + "' not found");
    }

    @Override
    protected PowsyblException createNotConnectedException() {
        return new PowsyblException("VoltageLevel '" + getVoltageLevelId() + "' not connected to line '" + getBranchId() + "'");
    }
}
