/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria;

import org.apache.commons.lang3.DoubleRange;

import java.util.Optional;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public final class VoltageInterval {
    private final Double nominalVoltageLowBound;
    private final Double nominalVoltageHighBound;
    private final boolean lowClosed;
    private final boolean highClosed;

    /**
     * Create a new {@link VoltageInterval} to filter network elements
     * which nominal voltages are inside a given interval.
     *
     * @param lowBound   lower bound of the acceptable interval. It may be <code>null</code>, if the interval has no lower bound.
     * @param highBound  upper bound of the acceptable interval. It may be <code>null</code>, if the interval has no upper bound.
     * @param lowClosed  <code>true</code> if <code>lowBound</code> is part of the interval, <code>false</code> otherwise.
     * @param highClosed <code>true</code> if <code>highBound</code> is part of the interval, <code>false</code> otherwise.
     */
    private VoltageInterval(Double lowBound, Double highBound, boolean lowClosed, boolean highClosed) {
        this.nominalVoltageLowBound = lowBound;
        this.nominalVoltageHighBound = highBound;
        this.lowClosed = lowClosed;
        this.highClosed = highClosed;
    }

    public static class Builder {
        private Double nominalVoltageLowBound = null;
        private Double nominalVoltageHighBound = null;
        private boolean lowClosed = true;
        private boolean highClosed = true;

        /**
         * Define the lower bound of the interval.
         *
         * @param value  value of the lower bound.
         * @param closed <code>true</code> if the bound is part of the interval, <code>false</code> otherwise.
         * @return the current builder
         */
        public Builder setLowBound(double value, boolean closed) {
            checkValue(value);
            checkBounds(value, nominalVoltageHighBound, closed, highClosed);
            this.nominalVoltageLowBound = value;
            this.lowClosed = closed;
            return this;
        }

        /**
         * Define the upper bound of the interval.
         *
         * @param value  value of the upper bound.
         * @param closed <code>true</code> if the bound is part of the interval, <code>false</code> otherwise.
         * @return the current builder
         */
        public Builder setHighBound(double value, boolean closed) {
            checkValue(value);
            checkBounds(nominalVoltageLowBound, value, lowClosed, closed);
            this.nominalVoltageHighBound = value;
            this.highClosed = closed;
            return this;
        }

        protected static void checkValue(double value) {
            if (Double.isNaN(value) || Double.isInfinite(value) || value < 0) {
                throw new IllegalArgumentException("Invalid interval bound value (must be >= 0 and not infinite).");
            }
        }

        protected static void checkBounds(Double low, Double high, boolean closedLow, boolean closedHigh) {
            if (low != null && high != null && low > high) {
                throw new IllegalArgumentException("Invalid interval bounds values (nominalVoltageLowBound must be <= nominalVoltageHighBound).");
            }
            double l = low != null ? low : 0;
            double h = high != null ? high : Double.MAX_VALUE;
            if (l == h && (!closedLow || !closedHigh)) {
                throw new IllegalArgumentException("Invalid interval: it should not be empty");
            }
        }

        public VoltageInterval build() {
            if (nominalVoltageLowBound == null && nominalVoltageHighBound == null) {
                throw new IllegalArgumentException("Invalid interval: at least one bound must be defined.");
            }
            return new VoltageInterval(nominalVoltageLowBound, nominalVoltageHighBound, lowClosed, highClosed);
        }
    }

    /**
     * Return a builder to create an {@link VoltageInterval}.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * <p>Convenient method to easily create a {@link VoltageInterval} with only a lower bound.</p>
     * @param value the lower bound of the interval to create (it corresponds to the <code>nominalVoltageLowBound</code> attribute of the interval)
     * @param valueIncluded is the bound included in the interval (it corresponds to the <code>lowClosed</code> attribute of the interval)
     * @return an interval
     */
    public static VoltageInterval greaterThan(double value, boolean valueIncluded) {
        return VoltageInterval.builder()
                .setLowBound(value, valueIncluded)
                .build();
    }

    /**
     * <p>Convenient method to easily create a {@link VoltageInterval} with only a upper bound.</p>
     * @param value the upper bound of the interval to create (it corresponds to the <code>nominalVoltageHighBound</code> attribute of the interval)
     * @param valueIncluded is the bound included in the interval (it corresponds to the <code>highClosed</code> attribute of the interval)
     * @return an interval
     */
    public static VoltageInterval lowerThan(double value, boolean valueIncluded) {
        return VoltageInterval.builder()
                .setHighBound(value, valueIncluded)
                .build();
    }

    /**
     * <p>Convenient method to easily create a {@link VoltageInterval} with only a upper bound.</p>
     * @param lowBound the lower bound of the interval to create (it corresponds to the <code>nominalVoltageLowBound</code> attribute of the interval)
     * @param lowIncluded is the bound included in the interval (it corresponds to the <code>lowClosed</code> attribute of the interval)
     * @param highBound the upper bound of the interval to create (it corresponds to the <code>nominalVoltageHighBound</code> attribute of the interval)
     * @param highIncluded is the bound included in the interval (it corresponds to the <code>highClosed</code> attribute of the interval)
     * @return an interval
     */
    public static VoltageInterval between(double lowBound, double highBound, boolean lowIncluded, boolean highIncluded) {
        return VoltageInterval.builder()
                .setLowBound(lowBound, lowIncluded)
                .setHighBound(highBound, highIncluded)
                .build();
    }

    /**
     * <p>Check if a value is inside the interval.</p>
     * <p>It returns <code>false</code> if the given value is null.</p>
     *
     * @param value the value to test
     * @return <code>true</code> if the value is inside the interval, <code>false</code> otherwise.
     */
    public boolean checkIsBetweenBound(Double value) {
        if (value == null || value < 0) {
            return false;
        }
        boolean lowBoundOk = nominalVoltageLowBound == null || value > nominalVoltageLowBound
                || lowClosed && value.equals(nominalVoltageLowBound);
        boolean highBoundOk = nominalVoltageHighBound == null || value < nominalVoltageHighBound
                || highClosed && value.equals(nominalVoltageHighBound);
        return lowBoundOk && highBoundOk;
    }

    /**
     * Get the lower bound of the interval.
     *
     * @return lower bound of the acceptable interval, or <code>Optional.empty()</code> if the interval has no lower bound.
     */
    public Optional<Double> getNominalVoltageLowBound() {
        return Optional.ofNullable(nominalVoltageLowBound);
    }

    /**
     * Get the upper bound of the interval.
     *
     * @return upper bound of the acceptable interval, or <code>Optional.empty()</code> if the interval has no upper bound.
     */
    public Optional<Double> getNominalVoltageHighBound() {
        return Optional.ofNullable(nominalVoltageHighBound);
    }

    /**
     * Is the interval closed on the lower side?
     *
     * @return <code>true</code> if <code>lowBound</code> is part of the interval, <code>false</code> otherwise.
     */
    public boolean isLowClosed() {
        return lowClosed;
    }

    /**
     * Is the interval closed on the upper side?
     *
     * @return <code>true</code> if <code>highBound</code> is part of the interval, <code>false</code> otherwise.
     */
    public boolean isHighClosed() {
        return highClosed;
    }

    /**
     * <p>Return a {@link DoubleRange} representation of the interval.</p>
     *
     * @return the interval as a {@link DoubleRange}
     */
    public DoubleRange asRange() {
        double min = 0;
        if (nominalVoltageLowBound != null) {
            min = nominalVoltageLowBound;
            if (!lowClosed) {
                min = min + Math.ulp(min);
            }
        }
        double max = Double.MAX_VALUE;
        if (nominalVoltageHighBound != null) {
            max = nominalVoltageHighBound;
            if (!highClosed) {
                max = max - Math.ulp(max);
            }
        }
        return DoubleRange.of(min, max);
    }
}
