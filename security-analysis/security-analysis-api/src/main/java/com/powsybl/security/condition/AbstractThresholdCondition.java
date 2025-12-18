/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.security.condition;

import java.util.Objects;

/**
 * Condition triggered by a threshold
 *
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public abstract class AbstractThresholdCondition implements Condition {

    public enum ComparisonType {
        EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        NOT_EQUAL
    }

    public enum Variable {
        ACTIVE_POWER,
        REACTIVE_POWER,
        CURRENT,
        TARGET_P
    }

    // Threshold to be checked for this condition
    private final double threshold;
    // Equipment on which to check the threshold
    private final String equipmentId;
    // Comparison type for the threshold
    private final ComparisonType comparisonType;
    // Variable to be measured against the threshold
    private final Variable variable;

    protected AbstractThresholdCondition(String equipmentId, Variable variable, ComparisonType comparisonType, double threshold) {
        this.equipmentId = Objects.requireNonNull(equipmentId);
        this.variable = Objects.requireNonNull(variable);
        this.comparisonType = Objects.requireNonNull(comparisonType);
        this.threshold = threshold;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public Variable getVariable() {
        return variable;
    }
}
