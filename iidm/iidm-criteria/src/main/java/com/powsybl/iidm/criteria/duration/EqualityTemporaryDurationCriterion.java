/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

/**
 * Criterion used to filter temporary limits which acceptable durations are equal to a given value (in seconds).
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public final class EqualityTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    private final int durationEqualityValue;

    /**
     * Create a new {@link EqualityTemporaryDurationCriterion} to filter temporary limits
     * which acceptable durations are equal to a given value in seconds.
     * @param durationEqualityValue duration in seconds
     */
    public EqualityTemporaryDurationCriterion(int durationEqualityValue) {
        if (durationEqualityValue <= 0) {
            throw new IllegalArgumentException("Invalid value (must be > 0).");
        }
        this.durationEqualityValue = durationEqualityValue;
    }

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.EQUALITY;
    }

    /**
     * Value of acceptable duration for temporary limits matching this criterion.
     * @return Value of acceptable duration for temporary limits matching this criterion.
     */
    public int getDurationEqualityValue() {
        return durationEqualityValue;
    }

    @Override
    public boolean filter(int acceptableDuration) {
        return acceptableDuration == getDurationEqualityValue();
    }
}
