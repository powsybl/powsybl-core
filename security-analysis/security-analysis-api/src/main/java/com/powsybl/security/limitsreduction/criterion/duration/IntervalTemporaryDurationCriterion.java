/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.limitsreduction.criterion.duration;

/**
 * Describes interval temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public final class IntervalTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    private final Integer lowBound;
    private final Integer highBound;
    private final boolean lowClosed;
    private final boolean highClosed;

    private IntervalTemporaryDurationCriterion(Integer lowBound, Integer highBound, boolean lowClosed, boolean highClosed) {
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.lowClosed = lowClosed;
        this.highClosed = highClosed;
    }

    public static class Builder {
        private Integer lowBound = null;
        private Integer highBound = null;
        private boolean lowClosed = false;
        private boolean highClosed = false;

        public Builder setLowBound(int value, boolean closed) {
            checkValue(value);
            checkBounds(value, highBound);
            this.lowBound = value;
            this.lowClosed = closed;
            return this;
        }

        public Builder setHighBound(int value, boolean closed) {
            checkValue(value);
            checkBounds(lowBound, value);
            this.highBound = value;
            this.highClosed = closed;
            return this;
        }

        private void checkValue(int value) {
            if (value <= 0) {
                throw new IllegalArgumentException("Invalid bound value (must be > 0).");
            }
        }

        private void checkBounds(Integer low, Integer high) {
            if (low != null && high != null && low > high) {
                throw new IllegalArgumentException("Invalid interval bounds values (low must be <= high).");
            }
        }

        public IntervalTemporaryDurationCriterion build() {
            if (lowBound == null && highBound == null) {
                throw new IllegalArgumentException("Invalid interval criterion: at least one bound must be defined.");
            }
            return new IntervalTemporaryDurationCriterion(lowBound, highBound, lowClosed, highClosed);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.INTERVAL;
    }

    @Override
    public boolean filter(int acceptableDuration) {
        boolean lowBoundOk = lowBound == null || acceptableDuration > lowBound
                || lowClosed && acceptableDuration == lowBound;
        boolean highBoundOk = highBound == null || acceptableDuration < highBound
                || highClosed && acceptableDuration == highBound;
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
