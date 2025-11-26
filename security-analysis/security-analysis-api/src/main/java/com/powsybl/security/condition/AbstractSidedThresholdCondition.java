/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

/**
 * Condition on a sided equipment triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractSidedThresholdCondition<S> extends AbstractThresholdCondition {

    // Side of the equipment on which to check the threshold
    private final S side;

    protected AbstractSidedThresholdCondition(String equipmentId, Variable variable, ComparisonType comparisonType,
                                              double threshold, S side) {
        super(equipmentId, variable, comparisonType, threshold);
        this.side = side;
    }

    public S getSide() {
        return side;
    }
}
