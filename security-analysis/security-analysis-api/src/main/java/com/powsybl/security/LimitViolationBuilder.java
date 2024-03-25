/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.iidm.network.TwoSides;

import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

/**
 * A builder class for {@link LimitViolation}s.
 *
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class LimitViolationBuilder {

    private String subjectId;
    private String subjectName;
    private LimitViolationType type;
    private Double limit;
    private String limitName;
    private Integer duration;
    private float reduction = 1.0f;
    private Double value;
    private ThreeSides side;

    public LimitViolationBuilder type(LimitViolationType type) {
        this.type = requireNonNull(type);
        return this;
    }

    public LimitViolationBuilder subject(String subjectId) {
        this.subjectId = requireNonNull(subjectId);
        return this;
    }

    public LimitViolationBuilder subjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    public LimitViolationBuilder limitName(String name) {
        this.limitName = name;
        return this;
    }

    public LimitViolationBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public LimitViolationBuilder duration(int duration, TimeUnit unit) {
        this.duration = (int) unit.toSeconds(duration);
        return this;
    }

    public LimitViolationBuilder limit(double limit) {
        this.limit = limit;
        return this;
    }

    public LimitViolationBuilder value(double value) {
        this.value = value;
        return this;
    }

    public LimitViolationBuilder reduction(float reduction) {
        this.reduction = reduction;
        return this;
    }

    public LimitViolationBuilder side(ThreeSides side) {
        this.side = requireNonNull(side);
        return this;
    }

    public LimitViolationBuilder side(TwoSides side) {
        return side(requireNonNull(side).toThreeSides());
    }

    public LimitViolationBuilder side1() {
        return side(TwoSides.ONE);
    }

    public LimitViolationBuilder side2() {
        return side(TwoSides.TWO);
    }

    public LimitViolationBuilder current() {
        return type(LimitViolationType.CURRENT);
    }

    public LimitViolation build() {
        requireNonNull(type);
        requireNonNull(limit, "Violated limit value must be defined.");
        requireNonNull(value, "Violation value must be defined.");
        switch (type) {
            case ACTIVE_POWER:
            case APPARENT_POWER:
            case CURRENT:
                requireNonNull(duration, "Acceptable duration must be defined.");
                requireNonNull(side, "Violation side must be defined.");
                return new LimitViolation(subjectId, subjectName, type, limitName, duration, limit, reduction, value, side);
            case LOW_VOLTAGE:
            case HIGH_VOLTAGE:
            case LOW_SHORT_CIRCUIT_CURRENT:
            case HIGH_SHORT_CIRCUIT_CURRENT:
            case LOW_VOLTAGE_ANGLE:
            case HIGH_VOLTAGE_ANGLE:
                return new LimitViolation(subjectId, subjectName, type, limitName, Integer.MAX_VALUE, limit, reduction, value);
            default:
                throw new UnsupportedOperationException(String.format("Building %s limits is not supported.", type.name()));
        }
    }
}
