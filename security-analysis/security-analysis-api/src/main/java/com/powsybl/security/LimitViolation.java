/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.iidm.network.*;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * A generic representation of a violation of a network equipment security limit.
 * For example, it may represent a current overload on a line.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class LimitViolation extends AbstractExtendable<LimitViolation> {

    private final String subjectId;

    private final String subjectName;

    private final LimitViolationType limitType;

    private final double limit;

    private final String limitName;

    private final int acceptableDuration;

    private final double limitReduction;

    private final double value;

    private final ThreeSides side;

    private final ViolationLocation voltageLocation;

    /**
     * Create a new LimitViolation.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param subjectName        An optional name of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     * @param side               The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     */
    public LimitViolation(String subjectId, @Nullable String subjectName, LimitViolationType limitType, @Nullable String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value, @Nullable ThreeSides side, @Nullable ViolationLocation voltageLocation) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.subjectName = subjectName;
        this.limitType = Objects.requireNonNull(limitType);
        this.limitName = limitName;
        this.acceptableDuration = acceptableDuration;
        this.limit = limit;
        this.limitReduction = limitReduction;
        this.value = value;
        this.side = checkSide(limitType, side);
        this.voltageLocation = voltageLocation;
    }

    /**
     * Create a new LimitViolation.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param subjectName        An optional name of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     * @param side               The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     */
    public LimitViolation(String subjectId, @Nullable String subjectName, LimitViolationType limitType, @Nullable String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value, @Nullable ThreeSides side) {
        this(subjectId, subjectName, limitType, limitName, acceptableDuration, limit, limitReduction, value, side, null);
    }

    /**
     * Create a new LimitViolation without side.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param subjectName        An optional name of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     */
    public LimitViolation(String subjectId, @Nullable String subjectName, LimitViolationType limitType, @Nullable String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value) {
        this(subjectId, subjectName, limitType, limitName, acceptableDuration, limit, limitReduction, value, (ThreeSides) null);

    }

    /**
     * Create a new LimitViolation.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param subjectName        An optional name of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     * @param side               The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     */
    public LimitViolation(String subjectId, @Nullable String subjectName, LimitViolationType limitType, @Nullable String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value, TwoSides side) {
        this(subjectId, subjectName, limitType, limitName, acceptableDuration, limit, limitReduction, value, Objects.requireNonNull(side).toThreeSides());
    }

    /**
     * Create a new LimitViolation.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     * @param side               The side of the equipment where the violation occurred. May be {@code null} for non-branch, non-three windings transformer equipments.
     */
    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value, TwoSides side) {
        this(subjectId, null, limitType, limitName, acceptableDuration, limit, limitReduction, value, Objects.requireNonNull(side).toThreeSides());
    }

    /**
     * Create a new LimitViolation without side.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId          The identifier of the network equipment on which the violation occurred.
     * @param limitType          The type of limit which has been violated.
     * @param limitName          An optional name for the limit which has been violated.
     * @param acceptableDuration The acceptable duration, in seconds, associated to the current violation value. Only relevant for current limits.
     * @param limit              The value of the limit which has been violated.
     * @param limitReduction     The limit reduction factor used for violation detection.
     * @param value              The actual value of the physical value which triggered the detection of a violation.
     */
    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, int acceptableDuration,
                          double limit, double limitReduction, double value) {
        this(subjectId, null, limitType, limitName, acceptableDuration, limit, limitReduction, value, (ThreeSides) null);
    }

    /**
     * Create a new LimitViolation, for types other than current limits.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId      The identifier of the network equipment on which the violation occurred.
     * @param limitType      The type of limit which has been violated.
     * @param limit          The value of the limit which has been violated.
     * @param limitReduction The limit reduction factor used for violation detection.
     * @param value          The actual value of the physical value which triggered the detection of a violation.
     * @param voltageLocation    Detailed information about the location of the violation.
     */
    public LimitViolation(String subjectId, LimitViolationType limitType, double limit, double limitReduction, double value, ViolationLocation voltageLocation) {
        this(subjectId, null, limitType, null, Integer.MAX_VALUE, limit, limitReduction, value, null, voltageLocation);
    }

    /**
     * Create a new LimitViolation, for types other than current limits.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId      The identifier of the network equipment on which the violation occurred.
     * @param subjectName    An optional name of the network equipment on which the violation occurred.
     * @param limitType      The type of limit which has been violated.
     * @param limit          The value of the limit which has been violated.
     * @param limitReduction The limit reduction factor used for violation detection.
     * @param value          The actual value of the physical value which triggered the detection of a violation.
     */
    public LimitViolation(String subjectId, String subjectName, LimitViolationType limitType, double limit, double limitReduction, double value) {
        this(subjectId, subjectName, limitType, null, Integer.MAX_VALUE, limit, limitReduction, value, null, null);
    }

    /**
     * Create a new LimitViolation, for types other than current limits.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId      The identifier of the network equipment on which the violation occurred.
     * @param subjectName    An optional name of the network equipment on which the violation occurred.
     * @param limitType      The type of limit which has been violated.
     * @param limit          The value of the limit which has been violated.
     * @param limitReduction The limit reduction factor used for violation detection.
     * @param value          The actual value of the physical value which triggered the detection of a violation.
     * @param voltageLocation    Detailed information about the location of the violation.
     */
    public LimitViolation(String subjectId, String subjectName, LimitViolationType limitType, double limit, double limitReduction, double value, ViolationLocation voltageLocation) {
        this(subjectId, subjectName, limitType, null, Integer.MAX_VALUE, limit, limitReduction, value, null, voltageLocation);
    }

    /**
     * Create a new LimitViolation, for voltage angle limit.
     *
     * <p>According to the violation type, all parameters may not be mandatory. See constructor overloads for particular types.
     *
     * @param subjectId      The identifier of the network equipment on which the violation occurred.
     * @param limitType      The type of limit which has been violated.
     * @param limit          The value of the limit which has been violated.
     * @param limitReduction The limit reduction factor used for violation detection.
     * @param value          The actual value of the physical value which triggered the detection of a violation.
     */
    public LimitViolation(String subjectId, LimitViolationType limitType, double limit, double limitReduction, double value) {
        this(subjectId, null, limitType, null, Integer.MAX_VALUE, limit, limitReduction, value, null, null);
    }

    /**
     * The identifier of the network equipment on which the violation occurred.
     *
     * @return the identifier of the network equipment on which the violation occurred.
     */
    public String getSubjectId() {
        return subjectId;
    }

    /**
     * The identifier of the network equipment on which the violation occurred.
     *
     * @return the identifier of the network equipment on which the violation occurred.
     */
    public Optional<ViolationLocation> getViolationLocation() {
        return Optional.ofNullable(voltageLocation);
    }

    /**
     * The name of the network equipment on which the violation occurred.
     * May be {@code null}.
     *
     * @return the name of the network equipment on which the violation occurred.
     */
    @Nullable
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * The type of limit which has been violated.
     *
     * @return the type of limit which has been violated.
     */
    public LimitViolationType getLimitType() {
        return limitType;
    }

    /**
     * The value of the limit which has been violated.
     *
     * @return the value of the limit which has been violated.
     */
    public double getLimit() {
        return limit;
    }

    /**
     * The name of the limit which has been violated. May be {@code null}.
     *
     * @return the value of the limit which has been violated. May be {@code null}.
     */
    @Nullable
    public String getLimitName() {
        return limitName;
    }

    /**
     * The acceptable duration, in seconds, associated to the current violation value.
     * Only relevant for current limits.
     *
     * @return the acceptable duration, in seconds, associated to the current violation value.
     */
    public int getAcceptableDuration() {
        return acceptableDuration;
    }

    /**
     * The limit reduction factor used for violation detection.
     * For example when monitoring values above 95% of a given limit, this will return {@code 0.95}
     *
     * @return the limit reduction factor used for violation detection.
     */
    public double getLimitReduction() {
        return limitReduction;
    }

    /**
     * The actual value of the physical value which triggered the detection of a violation.
     *
     * @return the actual value of the physical value which triggered the detection of a violation.
     */
    public double getValue() {
        return value;
    }

    /**
     * The side of the equipment with two sides (like branch) where the violation occurred.
     *
     * @return the side of the equipment with two sides (like branch) where the violation occurred.
     */
    public TwoSides getSideAsTwoSides() {
        return Objects.requireNonNull(side).toTwoSides();
    }

    /**
     * The side of the equipment where the violation occurred. Will be {@code null} for equipments
     * other than branches and three windings transformers.
     *
     * @return the side of the equipment where the violation occurred. Will be {@code null} for equipments
     * other than branches and three windings transformers.
     */
    @Nullable
    public ThreeSides getSide() {
        return side;
    }

    private static ThreeSides checkSide(LimitViolationType limitType, ThreeSides side) {
        if (limitType == LimitViolationType.ACTIVE_POWER
            || limitType == LimitViolationType.APPARENT_POWER
            || limitType == LimitViolationType.CURRENT) {
            return Objects.requireNonNull(side);
        } else {
            return null;
        }
    }

    public String toString() {
        return "Subject id: " + this.subjectId + ", Subject name: " + this.subjectName + ", limitType: " +
                this.limitType + ", limit: " + this.limit + ", limitName: " + this.limitName +
                ", acceptableDuration: " + this.acceptableDuration + ", limitReduction: " + this.limitReduction +
                ", value: " + this.value + ", side: " + this.side + ", voltageLocation: " + this.voltageLocation;
    }
}
