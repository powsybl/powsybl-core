/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Branch;

import static java.util.Objects.requireNonNull;

/**
 * A builder class for {@link LimitViolation}s.
 *
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LimitViolationBuilder {

    private String subjectId;
    private LimitViolationType type;
    private Double limit;
    private String name;
    private Integer duration;
    private float reduction = 1.0f;
    private Double value;
    private Branch.Side side;

    public LimitViolationBuilder type(LimitViolationType type) {
        this.type = requireNonNull(type);
        return this;
    }

    public LimitViolationBuilder subject(String subjectId) {
        this.subjectId = requireNonNull(subjectId);
        return this;
    }

    public LimitViolationBuilder name(String name) {
        this.name = requireNonNull(name);
        return this;
    }

    public LimitViolationBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    public LimitViolationBuilder permanent() {
        return duration(Integer.MAX_VALUE);
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

    public LimitViolationBuilder side(Branch.Side side) {
        this.side = requireNonNull(side);
        return this;
    }

    public LimitViolationBuilder side1() {
        return side(Branch.Side.ONE);
    }

    public LimitViolationBuilder side2() {
        return side(Branch.Side.TWO);
    }

    public LimitViolationBuilder current() {
        return type(LimitViolationType.CURRENT);
    }

    public LimitViolation build() {
        requireNonNull(type);
        requireNonNull(limit, "Violated limit value must be defined.");
        requireNonNull(value, "Violation value must be defined.");
        switch (type) {
            case CURRENT:
                requireNonNull(duration, "Acceptable duration must be defined.");
                requireNonNull(side, "Violation side must be defined.");
                return new LimitViolation(subjectId, LimitViolationType.CURRENT, name, duration, limit, reduction, value, side);
            case LOW_VOLTAGE:
            case HIGH_VOLTAGE:
                return new LimitViolation(subjectId, type, limit, reduction, value);
            default:
                throw new UnsupportedOperationException(String.format("Building %s limits is not supported yet.", type.name()));
        }
    }

}
