/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.criteria.duration;

import static com.powsybl.iidm.criteria.duration.LimitDurationCriterion.LimitDurationType.TEMPORARY;

/**
 * Super class for criteria used to filter temporary limits.
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public abstract class AbstractTemporaryDurationCriterion implements LimitDurationCriterion {

    public enum TemporaryDurationCriterionType {
        ALL,
        EQUALITY,
        INTERVAL
    }

    public abstract TemporaryDurationCriterionType getComparisonType();

    @Override
    public LimitDurationType getType() {
        return TEMPORARY;
    }

    /**
     * Does the given acceptable duration respect the current duration criterion?
     * @param acceptableDuration the duration to check
     * @return <code>true</code> if the given acceptable duration respects the current criterion,
     *         <code>false</code> otherwise.
     */
    public abstract boolean filter(int acceptableDuration);

}
