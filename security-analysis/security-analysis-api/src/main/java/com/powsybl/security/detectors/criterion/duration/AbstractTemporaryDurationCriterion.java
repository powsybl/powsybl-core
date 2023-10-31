/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.duration;

import static com.powsybl.security.detectors.criterion.duration.LimitDurationCriterion.LimitDurationType.TEMPORARY;

/**
 * Describes temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

public abstract class AbstractTemporaryDurationCriterion implements LimitDurationCriterion {

    enum TemporaryDurationCriterionType {
        ALL,
        EQUALITY,
        INTERVAL
    }

    public static final LimitDurationType TYPE = TEMPORARY;

    public abstract TemporaryDurationCriterionType getComparisonType();

    @Override
    public LimitDurationType getType() {
        return TYPE;
    }

    public class AllTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
        private static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.ALL;

        @Override
        public TemporaryDurationCriterionType getComparisonType() {
            return COMPARISON_TYPE;
        }
    }

    public class EqualityTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
        private static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.EQUALITY;
        private double durationEqualityValue;

        public EqualityTemporaryDurationCriterion(double durationEqualityValue) {
            this.durationEqualityValue = durationEqualityValue;
        }

        @Override
        public TemporaryDurationCriterionType getComparisonType() {
            return COMPARISON_TYPE;
        }

        public double getDurationEqualityValue() {
            return durationEqualityValue;
        }
    }

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

}
