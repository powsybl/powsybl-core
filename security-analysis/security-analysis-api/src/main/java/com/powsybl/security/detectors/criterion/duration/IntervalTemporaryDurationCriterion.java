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
    private Integer lowBound = null;
    private Integer highBound = null;
    private boolean lowClosed = false;
    private boolean highClosed = false;

    public IntervalTemporaryDurationCriterion setLowBound(int value, boolean closed) {
        checkValue(value);
        checkBounds(value, highBound);
        this.lowBound = value;
        this.lowClosed = closed;
        return this;
    }

    public void resetLowBound() {
        this.lowBound = null;
        this.lowClosed = false;
    }

    public IntervalTemporaryDurationCriterion setHighBound(int value, boolean closed) {
        checkValue(value);
        checkBounds(lowBound, value);
        this.highBound = value;
        this.highClosed = closed;
        return this;
    }

    public void resetHighBound() {
        this.highBound = null;
        this.highClosed = false;
    }

    private void checkValue(int value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Invalid bound value (must be > 0)");
        }
    }

    private void checkBounds(Integer low, Integer high) {
        if (low != null && high != null && low > high) {
            throw new IllegalArgumentException("Invalid interval bounds values (low must be <= high)");
        }
    }

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.INTERVAL;
    }

    @Override
    public boolean isTemporaryLimitWithinCriterionBounds(LoadingLimits.TemporaryLimit temporaryLimit) {
        int temporaryLimitAcceptableDuration = temporaryLimit.getAcceptableDuration();
        boolean lowBoundOk = lowBound == null || temporaryLimitAcceptableDuration > lowBound
                || lowClosed && temporaryLimitAcceptableDuration == lowBound;
        boolean highBoundOk = highBound == null || temporaryLimitAcceptableDuration < highBound
                || highClosed && temporaryLimitAcceptableDuration == highBound;
        return lowBoundOk && highBoundOk;
    }

    public Integer getLowBound() {
        return lowBound;
    }

    public Integer getHighBound() {
        return highBound;
    }

    public boolean isLowClosed() {
        return lowClosed;
    }

    public boolean isHighClosed() {
        return highClosed;
    }
}
