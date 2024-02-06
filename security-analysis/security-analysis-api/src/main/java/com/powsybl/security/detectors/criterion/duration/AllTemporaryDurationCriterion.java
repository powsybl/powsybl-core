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
 * Describes all temporary duration criterion
 *
 * @author Sophie Frasnedo {@literal <sophie.frasnedo at rte-france.com>}
 */
public class AllTemporaryDurationCriterion extends AbstractTemporaryDurationCriterion {
    @Override
    public TemporaryDurationCriterionType getComparisonType() {
        return TemporaryDurationCriterionType.ALL;
    }

    @Override
    public boolean isTemporaryLimitWithinCriterionBounds(LoadingLimits.TemporaryLimit temporaryLimit) {
        return true;
    }
}
