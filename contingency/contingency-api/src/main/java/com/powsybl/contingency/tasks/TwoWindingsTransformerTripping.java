/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.tasks;

import com.powsybl.iidm.network.Network;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class TwoWindingsTransformerTripping extends BranchTripping {

    public TwoWindingsTransformerTripping(String lineId) {
        this(lineId, null);
    }

    public TwoWindingsTransformerTripping(String lineId, String voltageLevelId) {
        super(lineId, voltageLevelId, Network::getTwoWindingsTransformer);
    }

    protected String getLabel() {
        return "Two windings transformer";
    }
}
