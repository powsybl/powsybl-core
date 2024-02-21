/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction.criterion.duration;

/**
 * Describes equality temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class EqualityTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    private final int durationEqualityValue;

    public EqualityTemporaryDurationCriterion(int durationEqualityValue) {
        this.durationEqualityValue = durationEqualityValue;
    }

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.EQUALITY;
    }

    public int getDurationEqualityValue() {
        return durationEqualityValue;
    }

    @Override
    public boolean filter(int acceptableDuration) {
        return acceptableDuration == getDurationEqualityValue();
    }
}
