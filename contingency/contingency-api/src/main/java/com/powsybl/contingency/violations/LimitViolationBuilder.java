/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.violations;

import com.powsybl.iidm.network.OperationalLimitsGroup;
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
    private String operationalLimitsGroupId = "";
    private LimitViolationType type;
    private Double limit;
    private String limitName;
    private Integer duration = Integer.MAX_VALUE;
    private double reduction = 1.0;
    private Double value;
    private ThreeSides side;
    private ViolationLocation violationLocation;

    /**
     * @param type The type of limit which has been violated.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder type(LimitViolationType type) {
        this.type = requireNonNull(type);
        return this;
    }

    /**
     * @param subjectId The identifier of the network equipment on which the violation occurred.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder subject(String subjectId) {
        this.subjectId = requireNonNull(subjectId);
        return this;
    }

    /**
     * @param subjectName An optional name of the network equipment on which the violation occurred.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder subjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    /**
     * @param id The {@link OperationalLimitsGroup} due to which this violation occurred.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder operationalLimitsGroupId(String id) {
        this.operationalLimitsGroupId = id;
        return this;
    }

    /**
     * @param location Detailed information about the location of the violation.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder violationLocation(ViolationLocation location) {
        this.violationLocation = location;
        return this;
    }

    /**
     * @param name An optional name for the limit which has been violated.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder limitName(String name) {
        this.limitName = name;
        return this;
    }

    /**
     * @param duration The acceptable duration, in seconds, associated to the violation value.
     *                 Default is {@link Integer#MAX_VALUE}.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder duration(int duration) {
        this.duration = duration;
        return this;
    }

    /**
     * @param duration The acceptable duration, in <code>unit</code> of time, associated to the violation value.
     * @param unit the time unit to be used.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder duration(int duration, TimeUnit unit) {
        this.duration = (int) unit.toSeconds(duration);
        return this;
    }

    /**
     * @param limit The value of the limit which has been violated.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder limit(double limit) {
        this.limit = limit;
        return this;
    }

    /**
     * @param value The actual value of the physical value which triggered the detection of a violation.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder value(double value) {
        this.value = value;
        return this;
    }

    /**
     * @param reduction The limit reduction factor used for violation detection.
     *                  Default is 1.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder reduction(double reduction) {
        this.reduction = reduction;
        return this;
    }

    /**
     * @param side The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder side(ThreeSides side) {
        this.side = requireNonNull(side);
        return this;
    }

    /**
     * @param side side The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder side(TwoSides side) {
        return side(requireNonNull(side).toThreeSides());
    }

    /**
     * Sets the side as {@link ThreeSides#ONE}.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder side1() {
        return side(TwoSides.ONE);
    }

    /**
     * Sets the side as {@link ThreeSides#TWO}.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder side2() {
        return side(TwoSides.TWO);
    }

    /**
     * Sets the side as {@link ThreeSides#THREE}.
     * @return this {@link LimitViolationBuilder}
     */
    public LimitViolationBuilder side3() {
        return side(ThreeSides.THREE);
    }

    /**
     * Sets the violation type as {@link LimitViolationType#CURRENT}.
     * @return this {@link LimitViolationBuilder}
     */
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
                return new LimitViolation(subjectId, subjectName, operationalLimitsGroupId, type, limitName, duration, limit, reduction, value, side, violationLocation);
            case LOW_VOLTAGE:
            case HIGH_VOLTAGE:
            case LOW_SHORT_CIRCUIT_CURRENT:
            case HIGH_SHORT_CIRCUIT_CURRENT:
            case LOW_VOLTAGE_ANGLE:
            case HIGH_VOLTAGE_ANGLE:
                return new LimitViolation(subjectId, subjectName, operationalLimitsGroupId, type, limitName, Integer.MAX_VALUE, limit, reduction, value, null, violationLocation);
            default:
                throw new UnsupportedOperationException(String.format("Building %s limits is not supported.", type.name()));
        }
    }
}
