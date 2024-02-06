/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.duration;

import com.powsybl.iidm.network.LoadingLimits;

/**
 * Describes interval temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class IntervalTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    private static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.INTERVAL;
    private double lowBound;
    private double highBound;
    private boolean lowClosed;
    private boolean highClosed;

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return COMPARISON_TYPE;
    }

    @Override
    public boolean isTemporaryLimitWithinCriterionBounds(LoadingLimits.TemporaryLimit temporaryLimit) {
        int temporaryLimitAcceptableDuration = temporaryLimit.getAcceptableDuration();
        if (temporaryLimitAcceptableDuration < lowBound || temporaryLimitAcceptableDuration > highBound) {
            return false;
        }
        if (!lowClosed && temporaryLimitAcceptableDuration == lowBound) {
            return false;
        }
        if (!highClosed && temporaryLimitAcceptableDuration == highBound) {
            return false;
        }
        return true;
    }

    public double getLowBound() {
        return lowBound;
    }

    public IntervalTemporaryDurationCriterion setLowBound(double lowBound) {
        this.lowBound = lowBound;
        return this;
    }

    public double getHighBound() {
        return highBound;
    }

    public IntervalTemporaryDurationCriterion setHighBound(double highBound) {
        this.highBound = highBound;
        return this;
    }

    public boolean isLowClosed() {
        return lowClosed;
    }

    public IntervalTemporaryDurationCriterion setLowClosed(boolean lowClosed) {
        this.lowClosed = lowClosed;
        return this;
    }

    public boolean isHighClosed() {
        return highClosed;
    }

    public IntervalTemporaryDurationCriterion setHighClosed(boolean highClosed) {
        this.highClosed = highClosed;
        return this;
    }
}
