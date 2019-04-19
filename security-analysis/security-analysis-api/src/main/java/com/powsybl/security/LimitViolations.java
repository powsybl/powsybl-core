/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Branch;

import java.util.Comparator;

/**
 * Utility methods for {@link LimitViolation}s, in particular to ease their building and comparison.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public final class LimitViolations {

    private static final Comparator<LimitViolation> COMPARATOR = Comparator.comparing(LimitViolation::getLimitType)
            .thenComparing(LimitViolation::getSubjectId)
            .thenComparing(LimitViolation::getSide, Comparator.nullsFirst(Branch.Side::compareTo))
            .thenComparing(LimitViolation::getLimitName, Comparator.nullsFirst(String::compareTo))
            .thenComparing(LimitViolation::getAcceptableDuration)
            .thenComparing(LimitViolation::getLimit)
            .thenComparing(LimitViolation::getLimitReduction)
            .thenComparing(LimitViolation::getValue);

    private LimitViolations() {
    }

    /**
     * A comparator which compares limit violations without comparing extensions.
     */
    public static Comparator<LimitViolation> comparator() {
        return COMPARATOR;
    }

    /**
     * A builder for current limit violations.
     */
    public static LimitViolationBuilder current() {
        return new LimitViolationBuilder().type(LimitViolationType.CURRENT);
    }

    /**
     * A builder for high voltage limit violations.
     */
    public static LimitViolationBuilder highVoltage() {
        return new LimitViolationBuilder().type(LimitViolationType.HIGH_VOLTAGE);
    }

    /**
     * A builder for low voltage limit violations.
     */
    public static LimitViolationBuilder lowVoltage() {
        return new LimitViolationBuilder().type(LimitViolationType.LOW_VOLTAGE);
    }
}
