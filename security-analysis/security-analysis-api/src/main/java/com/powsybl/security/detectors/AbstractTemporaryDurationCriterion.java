/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors;

import static com.powsybl.security.detectors.LimitDurationCriterion.LimitDurationType.TEMPORARY;

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
        public static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.ALL;

        @Override
        public TemporaryDurationCriterionType getComparisonType() {
            return null;
        }
    }

    public class EqualityTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
        public static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.EQUALITY;
        private double value;

        @Override
        public TemporaryDurationCriterionType getComparisonType() {
            return null;
        }
    }

    public class IntervalTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
        public static final TemporaryDurationCriterionType COMPARISON_TYPE = TemporaryDurationCriterionType.INTERVAL;
        private double lowBound;
        private double highBound;
        private boolean lowClosed;
        private boolean highClosed;

        @Override
        public TemporaryDurationCriterionType getComparisonType() {
            return null;
        }
    }

}
