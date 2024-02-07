/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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

    public abstract boolean isAcceptableDurationWithinCriterionBounds(int acceptableDuration);

}
