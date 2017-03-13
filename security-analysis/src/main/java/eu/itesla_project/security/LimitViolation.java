/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import java.util.Collection;
import java.util.Objects;
import java.util.Properties;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Identifiable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolation {

    @Deprecated
    private final Identifiable subject;
    
    private final String subjectId;

    private final LimitViolationType limitType;

    private final float limit;

    private final String limitName;

    private final float limitReduction;

    private final float value;

    private final Country country;

    private final float baseVoltage;

    @Deprecated
    public LimitViolation(Identifiable subject, LimitViolationType limitType, float limit, String limitName, float limitReduction, float value, Country country, float baseVoltage) {
        this.subject = Objects.requireNonNull(subject);
        this.subjectId = subject.getId();
        this.limitType = Objects.requireNonNull(limitType);
        this.limit = limit;
        this.limitName = limitName;
        this.limitReduction = limitReduction;
        this.value = value;
        this.country = country;
        this.baseVoltage = baseVoltage;
    }
    
    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, String limitName, float limitReduction, float value, Country country, float baseVoltage) {
        this.subject = null;
        this.subjectId = Objects.requireNonNull(subjectId);
        this.limitType = Objects.requireNonNull(limitType);
        this.limit = limit;
        this.limitName = limitName;
        this.limitReduction = limitReduction;
        this.value = value;
        this.country = country;
        this.baseVoltage = baseVoltage;
    }

    @Deprecated
    public LimitViolation(Identifiable subject, LimitViolationType limitType, float limit, String limitName, float value) {
        this(subject, limitType, limit, limitName, 1, value, null, Float.NaN);
    }
    
    public LimitViolation(String subjectId, LimitViolationType limitType, float limit, String limitName, float value) {
        this(subjectId, limitType, limit, limitName, 1, value, null, Float.NaN);
    }

    @Deprecated
    public Identifiable getSubject() {
        return subject;
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

    public Country getCountry() {
        return country;
    }

    public float getBaseVoltage() {
        return baseVoltage;
    }

}
