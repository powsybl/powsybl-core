/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

import com.powsybl.iidm.network.ThreeSides;

/**
 * Condition on a branch triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class BranchThresholdCondition extends AbstractThresholdCondition {

    public static final String NAME = "BRANCH_THRESHOLD_CONDITION";

    // Side of the equipment on which to check the threshold
    private final ThreeSides side;

    public BranchThresholdCondition(double threshold, AbstractThresholdCondition.ComparisonType type, String equipmentId,
                                    AbstractThresholdCondition.Variable variable, ThreeSides side) {
        super(threshold, type, equipmentId, variable);
        this.side = side;
    }

    public ThreeSides getSide() {
        return side;
    }

    @Override
    public String getType() {
        return NAME;
    }
}
