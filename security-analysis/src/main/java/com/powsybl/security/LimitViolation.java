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

    private final String subjectId;

    private final LimitViolationType limitType;

    private final float limit;

    private final String limitName;

    private final float limitReduction;

    private final float value;

    private final Country country;

    private final float baseVoltage;

    private final Branch.Side side;

    /**
     * @deprecated use LimitViolation(String, LimitViolationType, String, float, float, float) instead.
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
        this.country = country;
        this.baseVoltage = baseVoltage;
        this.side = null;
    }

    /**
     * @deprecated use LimitViolation(String, LimitViolationType, String, float, float, float) instead.
     */
    @Deprecated
    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, String limitName, float value) {
        this(subjectId, limitType, limit, limitName, 1, value, null, Float.NaN);
    }

    public LimitViolation(String subjectId, LimitViolationType limitType, String limitName, float limit, float limitReduction,
                          float value, Branch.Side side) {
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limitName = limitName;
        this.limit = limit;
        this.limitReduction = limitReduction;
        this.value = value;
        this.side = checkSide(limitType, side);
        this.country = null;
        this.baseVoltage = Float.NaN;
    }

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

    public Branch.Side getSide() {
        return side;
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

    static Country getCountry(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        Country country;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            country = branch.getTerminal(limitViolation.getSide()).getVoltageLevel().getSubstation().getCountry();
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
            throw new AssertionError("Unexpected identifiable type: " + identifiable.getClass());
        }

        return country;
    }

    static String getVoltageLevelName(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        String voltageLevelName;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            voltageLevelName = branch.getTerminal(limitViolation.getSide()).getVoltageLevel().getName();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            voltageLevelName = injection.getTerminal().getVoltageLevel().getName();
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            voltageLevelName = voltageLevel.getName();
        } else if (identifiable instanceof Bus) {
            Bus bus = (Bus) identifiable;
            voltageLevelName = bus.getVoltageLevel().getName();
        } else {
            throw new AssertionError("Unexpected identifiable type: " + identifiable.getClass());
        }

        return voltageLevelName;
    }

    static float getNominalVoltage(LimitViolation limitViolation, Network network) {
        Objects.requireNonNull(limitViolation);
        Objects.requireNonNull(network);

        float nominalVoltage;

        Identifiable identifiable = network.getIdentifiable(limitViolation.getSubjectId());
        if (identifiable == null) {
            throw new AssertionError("Unable to find the identifiable: " + limitViolation.getSubjectId());
        } else if (identifiable instanceof Branch) {
            Branch branch = (Branch) identifiable;
            nominalVoltage = branch.getTerminal(limitViolation.getSide()).getVoltageLevel().getNominalV();
        } else if (identifiable instanceof Injection) {
            Injection injection = (Injection) identifiable;
            nominalVoltage = injection.getTerminal().getVoltageLevel().getNominalV();
        } else if (identifiable instanceof VoltageLevel) {
            VoltageLevel voltageLevel = (VoltageLevel) identifiable;
            nominalVoltage = voltageLevel.getNominalV();
        } else {
            throw new AssertionError("Unexpected identifiable type: " + identifiable.getClass());
        }

        return nominalVoltage;
    }
}
