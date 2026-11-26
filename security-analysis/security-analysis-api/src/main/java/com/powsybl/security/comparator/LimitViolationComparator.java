/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.comparator;

import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.contingency.violations.LimitViolationType;
import com.powsybl.iidm.network.ThreeSides;

import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
public class LimitViolationComparator implements Comparator<LimitViolation> {

    private static final Comparator<LimitViolation> BASE_COMPARATOR = Comparator.comparing(LimitViolation::getSubjectId)
        // The operational limits group is null for violations not related to any group (e.g. in detailed DC network)
        .thenComparing(LimitViolation::getOperationalLimitsGroupId, Comparator.nullsFirst(Comparator.naturalOrder()))
        .thenComparing(LimitViolation::getLimitType);

    // The side is null for violations on equipments without sides (e.g. DC lines)
    private static final Comparator<ThreeSides> SIDE_COMPARATOR = Comparator.nullsFirst(Comparator.naturalOrder());

    @Override
    public int compare(LimitViolation violation1, LimitViolation violation2) {
        Objects.requireNonNull(violation1);
        Objects.requireNonNull(violation2);
        int baseComparison = BASE_COMPARATOR.compare(violation1, violation2);
        if (baseComparison != 0) {
            return baseComparison;
        }

        if (LimitViolationType.CURRENT == violation1.getLimitType()
            || LimitViolationType.ACTIVE_POWER == violation1.getLimitType()
            || LimitViolationType.APPARENT_POWER == violation1.getLimitType()) {
            return SIDE_COMPARATOR.compare(violation1.getSide(), violation2.getSide());
        }
        return 0;

    }

}
