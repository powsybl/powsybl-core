/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.detectors.criterion.duration;

/**
 * Describes equality temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */

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
