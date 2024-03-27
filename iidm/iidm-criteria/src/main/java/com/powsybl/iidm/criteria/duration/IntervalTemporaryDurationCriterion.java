/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import org.apache.commons.lang3.IntegerRange;

import java.util.Optional;

/**
 * Criterion used to filter temporary limits which acceptable durations are inside a given interval (in seconds).
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public final class IntervalTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    private final Integer lowBound;
    private final Integer highBound;
    private final boolean lowClosed;
    private final boolean highClosed;

    /**
     * Create a new {@link IntervalTemporaryDurationCriterion} to filter temporary limits
     * which acceptable durations inside a given interval (in seconds).
     * @param lowBound lower bound of the acceptable interval (in seconds). It may be <code>null</code>, if the interval has no lower bound.
     * @param highBound upper bound of the acceptable interval (in seconds). It may be <code>null</code>, if the interval has no upper bound.
     * @param lowClosed <code>true</code> if <code>lowBound</code> is part of the interval, <code>false</code> otherwise.
     * @param highClosed <code>true</code> if <code>highBound</code> is part of the interval, <code>false</code> otherwise.
     */
    private IntervalTemporaryDurationCriterion(Integer lowBound, Integer highBound, boolean lowClosed, boolean highClosed) {
        this.lowBound = lowBound;
        this.highBound = highBound;
        this.lowClosed = lowClosed;
        this.highClosed = highClosed;
    }

    public static class Builder {
        private Integer lowBound = null;
        private Integer highBound = null;
        private boolean lowClosed = true;
        private boolean highClosed = true;

        /**
         * Define the lower bound of the interval.
         * @param value value of the lower bound (in seconds).
         * @param closed <code>true</code> if the bound is part of the interval, <code>false</code> otherwise.
         * @return the current builder
         */
        public Builder setLowBound(int value, boolean closed) {
            checkValue(value);
            checkBounds(value, highBound, closed, highClosed);
            this.lowBound = value;
            this.lowClosed = closed;
            return this;
        }

        /**
         * Define the upper bound of the interval.
         * @param value value of the upper bound (in seconds).
         * @param closed <code>true</code> if the bound is part of the interval, <code>false</code> otherwise.
         * @return the current builder
         */
        public Builder setHighBound(int value, boolean closed) {
            checkValue(value);
            checkBounds(lowBound, value, lowClosed, closed);
            this.highBound = value;
            this.highClosed = closed;
            return this;
        }

        private void checkValue(int value) {
            if (value < 0) {
                throw new IllegalArgumentException("Invalid bound value (must be >= 0).");
            }
        }

        private void checkBounds(Integer low, Integer high, boolean closedLow, boolean closedHigh) {
            if (low != null && high != null && low > high) {
                throw new IllegalArgumentException("Invalid interval bounds values (low must be <= high).");
            }
            int l = low != null ? low : 0;
            int h = high != null ? high : Integer.MAX_VALUE;
            if (l == h && (!closedLow || !closedHigh)) {
                throw new IllegalArgumentException("Invalid interval: it should not be empty");
            }
        }

        public IntervalTemporaryDurationCriterion build() {
            if (lowBound == null && highBound == null) {
                throw new IllegalArgumentException("Invalid interval criterion: at least one bound must be defined.");
            }
            return new IntervalTemporaryDurationCriterion(lowBound, highBound, lowClosed, highClosed);
        }
    }

    /**
     * Return a builder to create an {@link IntervalTemporaryDurationCriterion}.
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Convenient method to easily create an {@link IntervalTemporaryDurationCriterion} with only a lower bound.</p>
     * @param value the lower bound (in seconds) of the interval to create (it corresponds to the <code>lowBound</code> attribute of the interval)
     * @param valueIncluded is the bound included in the interval (it corresponds to the <code>lowClosed</code> attribute of the interval)
     * @return an interval
     */
    public static IntervalTemporaryDurationCriterion greaterThan(int value, boolean valueIncluded) {
        return IntervalTemporaryDurationCriterion.builder()
                .setLowBound(value, valueIncluded)
                .build();
    }

    /**
     * <p>Convenient method to easily create an {@link IntervalTemporaryDurationCriterion} with only a upper bound.</p>
     * @param value the upper bound (in seconds) of the interval to create (it corresponds to the <code>highBound</code> attribute of the interval)
     * @param valueIncluded is the bound included in the interval (it corresponds to the <code>highClosed</code> attribute of the interval)
     * @return an interval
     */
    public static IntervalTemporaryDurationCriterion lowerThan(int value, boolean valueIncluded) {
        return IntervalTemporaryDurationCriterion.builder()
                .setHighBound(value, valueIncluded)
                .build();
    }

    /**
     * <p>Convenient method to easily create an {@link IntervalTemporaryDurationCriterion} with only a upper bound.</p>
     * @param lowBound the lower bound (in seconds) of the interval to create (it corresponds to the <code>lowBound</code> attribute of the interval)
     * @param lowIncluded is the bound included in the interval (it corresponds to the <code>lowClosed</code> attribute of the interval)
     * @param highBound the upper bound (in seconds) of the interval to create (it corresponds to the <code>highBound</code> attribute of the interval)
     * @param highIncluded is the bound included in the interval (it corresponds to the <code>highClosed</code> attribute of the interval)
     * @return an interval
     */
    public static IntervalTemporaryDurationCriterion between(int lowBound, int highBound, boolean lowIncluded, boolean highIncluded) {
        return IntervalTemporaryDurationCriterion.builder()
                .setLowBound(lowBound, lowIncluded)
                .setHighBound(highBound, highIncluded)
                .build();
    }

    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.INTERVAL;
    }

    @Override
    public boolean filter(int acceptableDuration) {
        if (acceptableDuration < 0) {
            return false;
        }
        boolean lowBoundOk = lowBound == null || acceptableDuration > lowBound
                || lowClosed && acceptableDuration == lowBound;
        boolean highBoundOk = highBound == null || acceptableDuration < highBound
                || highClosed && acceptableDuration == highBound;
        return lowBoundOk && highBoundOk;
    }

    /**
     * Get the lower bound (in seconds) of the interval.
     * @return lower bound of the acceptable interval (in seconds), or <code>Optional.empty()</code> if the interval has no lower bound.
     */
    public Optional<Integer> getLowBound() {
        return Optional.ofNullable(lowBound);
    }

    /**
     * Get the upper bound (in seconds) of the interval.
     * @return upper bound of the acceptable interval (in seconds), or <code>Optional.empty()</code> if the interval has no upper bound.
     */
    public Optional<Integer> getHighBound() {
        return Optional.ofNullable(highBound);
    }

    /**
     * Is the interval closed on the lower side?
     * @return <code>true</code> if <code>lowBound</code> is part of the interval, <code>false</code> otherwise.
     */
    public boolean isLowClosed() {
        return lowClosed;
    }

    /**
     * Is the interval closed on the upper side?
     * @return <code>true</code> if <code>highBound</code> is part of the interval, <code>false</code> otherwise.
     */
    public boolean isHighClosed() {
        return highClosed;
    }

    /**
     * <p>Return an {@link IntegerRange} representation corresponding to the criterion's interval.</p>
     * @return the criterion's interval as an {@link IntegerRange}
     */
    public IntegerRange asRange() {
        int min = 0;
        if (lowBound != null) {
            min = lowBound;
            if (!lowClosed) {
                min++;
            }
        }
        int max = Integer.MAX_VALUE;
        if (highBound != null) {
            max = highBound;
            if (!highClosed) {
                max--;
            }
        }
        return IntegerRange.of(min, max);
    }
}
