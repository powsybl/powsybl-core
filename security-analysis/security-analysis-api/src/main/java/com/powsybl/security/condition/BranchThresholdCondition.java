/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

import com.powsybl.iidm.network.TwoSides;

/**
 * Condition on a branch equipment triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class BranchThresholdCondition extends AbstractSidedThresholdCondition<TwoSides> {

    public static final String NAME = "BRANCH_THRESHOLD_CONDITION";

    public BranchThresholdCondition(String equipmentId, Variable variable, ComparisonType comparisonType,
                                    double threshold, TwoSides side) {
        super(equipmentId, variable, comparisonType, threshold, side);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
