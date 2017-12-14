/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.*;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolation {

    private static final String UNEXPECTED_IDENTIFIABLE_TYPE = "Unexpected identifiable type: ";
    private static final String IDENTIFIABLE_NOT_FOUND = "Unable to find the identifiable: ";
    private static final String REGIONCVG = "regionCvg";

    private final String subjectId;

    private final LimitViolationType limitType;

    private final float limit;

    private final String limitName;

    private final float limitReduction;

    private final float value;

    private float valueMW;

    private final Country country;

    private final float baseVoltage;

    private final Branch.Side side;

    private float valueBefore;

    private float valueBeforeMW;

    private int acceptableDuration;

    /**
     * @deprecated use LimitViolation(String, LimitViolationType, String, float, float, float, Branch.Side) instead.
     */
    @Deprecated
    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, String limitName,
                          float limitReduction, float value, Country country, float baseVoltage) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limit = limit;
        this.limitName = limitName;
        this.limitReduction = limitReduction;
        this.value = value;
        this.valueMW = Float.NaN;
        this.country = country;
        this.baseVoltage = baseVoltage;
        this.side = null;
        this.valueBefore = Float.NaN;
        this.valueBeforeMW = Float.NaN;
        this.acceptableDuration = Integer.MAX_VALUE;
    }

    /**
     * @deprecated use LimitViolation(String, LimitViolationType, String, float, float, float, Branch.Side) instead.
     */
    @Deprecated
    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, String limitName, float value) {
        this(subjectId, limitType, limit, limitName, 1, value, null, Float.NaN);
    }

    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, float limit, float limitReduction,
                          float value, Branch.Side side) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limit = limit;
        this.limitName = limitName;
        this.limitReduction = limitReduction;
        this.value = value;
        this.valueMW = Float.NaN;
        this.country = null;
        this.baseVoltage = Float.NaN;
        this.side = checkSide(limitType, side);
        this.valueBefore = Float.NaN;
        this.valueBeforeMW = Float.NaN;
        this.acceptableDuration = Integer.MAX_VALUE;
    }

    public static LimitViolation newLimitViolation(String subjectId, LimitViolationType limitType, String limitName, float limit, float limitReduction,
                                                   float value, Branch.Side side) {
        return new LimitViolation(subjectId, limitType, limitName, limit, limitReduction, value, side);
    }

    /*
    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, float limit,
                          float limitReduction, float value, float valueMW, Branch.Side side, float valueBefore,
                          float valueBeforeMW, int acceptableDuration) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limitName = limitName;
        this.limit = limit;
        this.limitReduction = limitReduction;
        this.value = value;
        this.valueMW = valueMW;
        this.side = checkSide(limitType, side);
        this.country = null;
        this.baseVoltage = Float.NaN;
        this.valueBefore = valueBefore;
        this.valueBeforeMW = valueBeforeMW;
        this.acceptableDuration = acceptableDuration;
    }
     */

    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, float limitReduction, float value) {
        this(subjectId, limitType, null, limit, limitReduction, value, null);
    }

    public String getSubjectId() {
        return subjectId;
    }

    public LimitViolationType getLimitType() {
        return limitType;
    }

    public float getLimit() {
        return limit;
    }

    public String getLimitName() {
        return limitName;
    }

    public float getLimitReduction() {
        return limitReduction;
    }

    public float getValue() {
        return value;
    }

    public float getValueMW() {
        return valueMW;
    }

    public LimitViolation setValueMW(float valueMW) {
        this.valueMW = valueMW;
        return this;
    }

    public Branch.Side getSide() {
        return side;
    }

    public float getValueBefore() {
        return valueBefore;
    }

    public LimitViolation setValueBefore(float valueBefore) {
        this.valueBefore = valueBefore;
        return this;
    }

    public float getValueBeforeMW() {
        return valueBeforeMW;
    }

    public LimitViolation setValueBeforeMW(float valueBeforeMW) {
        this.valueBeforeMW = valueBeforeMW;
        return this;
    }

    public int getAcceptableDuration() {
        return acceptableDuration;
    }

    public LimitViolation setAcceptableDuration(int acceptableDuration) {
        this.acceptableDuration = acceptableDuration;
        return this;
    }

    /**
     * @deprecated Use LimitViolation.getCountry(LimitViolation, Network) instead.
     */
    @Deprecated
    public Country getCountry() {
        return country;
    }

    /**
     * @deprecated Use LimitViolation.getNominalVoltage(LimitViolation, Network) instead.
     */
    @Deprecated
    public float getBaseVoltage() {
        return baseVoltage;
    }

    private static Branch.Side checkSide(LimitViolationType limitType, Branch.Side side) {
        if (limitType == LimitViolationType.CURRENT) {
            return Objects.requireNonNull(side);
        } else {
            return null;
        }
    }

    public static Country getCountry(LimitViolation limitViolation, Network network, Branch.Side side) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        Country country;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            country = branch.getTerminal(side).getVoltageLevel().getSubstation().getCountry();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            country = injection.getTerminal().getVoltageLevel().getSubstation().getCountry();
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            country = voltageLevel.getSubstation().getCountry();
        } else if (identifiable instanceof Bus) {
            Bus bus = (Bus) identifiable;
            country = bus.getVoltageLevel().getSubstation().getCountry();
        } else {
            throw new AssertionError(UNEXPECTED_IDENTIFIABLE_TYPE + identifiable.getClass());
        }

        return country;
    }

    public static Country getCountry(LimitViolation limitViolation, Network network) {
        return getCountry(limitViolation, network, limitViolation.getSide());
    }

    public static float getNominalVoltage(LimitViolation limitViolation, Network network, Branch.Side side) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        float nominalVoltage;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable == null) {
            throw new AssertionError(IDENTIFIABLE_NOT_FOUND + limitViolation.getSubjectId());
        } else if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            nominalVoltage = branch.getTerminal(side).getVoltageLevel().getNominalV();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            nominalVoltage = injection.getTerminal().getVoltageLevel().getNominalV();
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            nominalVoltage = voltageLevel.getNominalV();
        } else {
            throw new AssertionError(UNEXPECTED_IDENTIFIABLE_TYPE + identifiable.getClass());
        }

        return nominalVoltage;
    }

    public static float getNominalVoltage(LimitViolation limitViolation, Network network) {
        return getNominalVoltage(limitViolation, network, limitViolation.getSide());
    }

    public static String getRegion(LimitViolation limitViolation, Network network, Branch.Side side) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        String region = null;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable == null) {
            throw new AssertionError(IDENTIFIABLE_NOT_FOUND + limitViolation.getSubjectId());
        } else if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            region = branch.getTerminal(side).getVoltageLevel().getSubstation().getProperties().getProperty(REGIONCVG);
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            region = injection.getTerminal().getVoltageLevel().getSubstation().getProperties().getProperty(REGIONCVG);
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            region = voltageLevel.getSubstation().getProperties().getProperty(REGIONCVG);
        } else {
            throw new AssertionError(UNEXPECTED_IDENTIFIABLE_TYPE + identifiable.getClass());
        }

        return region;
    }

    public static String getRegion(LimitViolation limitViolation, Network network) {
        return getRegion(limitViolation, network, limitViolation.getSide());
    }

    public static String getSubstation(LimitViolation limitViolation, Network network, Branch.Side side) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        String substation = null;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable == null) {
            throw new AssertionError(IDENTIFIABLE_NOT_FOUND + limitViolation.getSubjectId());
        } else if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            substation = branch.getTerminal(side).getVoltageLevel().getName();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            substation = injection.getTerminal().getVoltageLevel().getName();
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            substation = voltageLevel.getName();
        } else {
            throw new AssertionError(UNEXPECTED_IDENTIFIABLE_TYPE + identifiable.getClass());
        }

        return substation;
    }

    public static String getSubstation(LimitViolation limitViolation, Network network) {
        return getSubstation(limitViolation, network, limitViolation.getSide());
    }
}
