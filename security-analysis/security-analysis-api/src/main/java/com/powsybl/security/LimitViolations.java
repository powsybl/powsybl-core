/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.ThreeSides;

import java.util.Comparator;

/**
 * Utility methods for {@link LimitViolation}s, in particular to ease their building and comparison.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public final class LimitViolations {

    private static final Comparator<LimitViolation> COMPARATOR = Comparator.comparing(LimitViolation::getLimitType)
            .thenComparing(LimitViolation::getSubjectId)
            .thenComparing(LimitViolation::getSide, Comparator.nullsFirst(ThreeSides::compareTo))
            .thenComparing(LimitViolation::getLimitName, Comparator.nullsFirst(String::compareTo))
            .thenComparing(LimitViolation::getAcceptableDuration)
            .thenComparing(LimitViolation::getLimit)
            .thenComparing(LimitViolation::getValue)
            .thenComparing(LimitViolation::getLimitReduction);

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
        return new LimitViolationBuilder().current();
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

    /**
     * A builder for low voltageAngle limit violations.
     */
    public static LimitViolationBuilder lowVoltageAngle() {
        return new LimitViolationBuilder().type(LimitViolationType.LOW_VOLTAGE_ANGLE);
    }

    /**
     * A builder for high voltageAngle limit violations.
     */
    public static LimitViolationBuilder highVoltageAngle() {
        return new LimitViolationBuilder().type(LimitViolationType.HIGH_VOLTAGE_ANGLE);
    }

    /**
     * A builder for high short-circuit current limit violations.
     */
    public static LimitViolationBuilder highShortCircuitCurrent() {
        return new LimitViolationBuilder().type(LimitViolationType.HIGH_SHORT_CIRCUIT_CURRENT);
    }

    /**
     * A builder for active power limit violations.
     */
    public static LimitViolationBuilder activePower() {
        return new LimitViolationBuilder().type(LimitViolationType.ACTIVE_POWER);
    }

    /**
     * A builder for apparent power limit violations.
     */
    public static LimitViolationBuilder apparentPower() {
        return new LimitViolationBuilder().type(LimitViolationType.APPARENT_POWER);
    }

    /**
     * A builder for low short-circuit current limit violations.
     */
    public static LimitViolationBuilder lowShortCircuitCurrent() {
        return new LimitViolationBuilder().type(LimitViolationType.LOW_SHORT_CIRCUIT_CURRENT);
    }
}
