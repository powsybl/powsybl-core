/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.condition;

/**
 * Condition on an injection triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class InjectionThresholdCondition extends AbstractThresholdCondition {

    public static final String NAME = "INJECTION_THRESHOLD_CONDITION";

    public InjectionThresholdCondition(String equipmentId, Variable variable, ComparisonType comparisonType,
                                       double threshold) {
        super(equipmentId, variable, comparisonType, threshold);
    }

    @Override
    public String getType() {
        return NAME;
    }
}
