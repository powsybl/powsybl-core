/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.iidm.network.Country;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilter {

    private static final Set<LimitViolationType> DEFAULT_VIOLATION_TYPES = null;
    private static final float DEFAULT_MIN_BASE_VOLTAGE = 0f;
    private static final Set<Country> DEFAULT_COUNTRIES = null;

    private static Set<LimitViolationType> checkViolationTypes(Set<LimitViolationType> violationTypes) {
        if (violationTypes != null && violationTypes.isEmpty()) {
            throw new IllegalArgumentException("Bad violation types filter");
        }
        return violationTypes;
    }

    private static float checkMinBaseVoltage(float minBaseVoltage) {
        if (Float.isNaN(minBaseVoltage) || minBaseVoltage < 0) {
            throw new IllegalArgumentException("Bad min base voltage filter " + minBaseVoltage);
        }
        return minBaseVoltage;
    }

    private static Set<Country> checkCountries(Set<Country> countries) {
        if (countries != null && countries.isEmpty()) {
            throw new IllegalArgumentException("Bad countries filter");
        }
        return countries;
    }

    public static LimitViolationFilter load() {
        return load(PlatformConfig.defaultConfig());
    }

    static LimitViolationFilter load(PlatformConfig platformConfig) {
        LimitViolationFilter filter = new LimitViolationFilter();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("limit-violation-default-filter");
        if (moduleConfig != null) {
            filter.setViolationTypes(moduleConfig.getEnumSetProperty("violationTypes", LimitViolationType.class, DEFAULT_VIOLATION_TYPES));
            filter.setMinBaseVoltage(moduleConfig.getFloatProperty("minBaseVoltage", DEFAULT_MIN_BASE_VOLTAGE));
            filter.setCountries(moduleConfig.getEnumSetProperty("countries", Country.class, DEFAULT_COUNTRIES));
        }
        return filter;
    }

    private Set<LimitViolationType> violationTypes;

    private float minBaseVoltage;

    private Set<Country> countries;

    public LimitViolationFilter(Set<LimitViolationType> violationTypes) {
        this(violationTypes, DEFAULT_MIN_BASE_VOLTAGE, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter() {
        this(DEFAULT_VIOLATION_TYPES, DEFAULT_MIN_BASE_VOLTAGE, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, float minBaseVoltage) {
        this(violationTypes, minBaseVoltage, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, float minBaseVoltage, Set<Country> countries) {
        this.violationTypes = checkViolationTypes(violationTypes);
        this.minBaseVoltage = checkMinBaseVoltage(minBaseVoltage);
        this.countries = checkCountries(countries);
    }

    public Set<LimitViolationType> getViolationTypes() {
        return violationTypes;
    }

    public LimitViolationFilter setViolationTypes(Set<LimitViolationType> violationTypes) {
        this.violationTypes = checkViolationTypes(violationTypes);
        return this;
    }

    public float getMinBaseVoltage() {
        return minBaseVoltage;
    }

    public LimitViolationFilter setMinBaseVoltage(float minBaseVoltage) {
        this.minBaseVoltage = checkMinBaseVoltage(minBaseVoltage);
        return this;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public LimitViolationFilter setCountries(Set<Country> countries) {
        this.countries = checkCountries(countries);
        return this;
    }

    public List<LimitViolation> apply(List<LimitViolation> violations) {
        return violations.stream()
                .filter(violation -> violationTypes == null || violationTypes.contains(violation.getLimitType()))
                .filter(violation -> Float.isNaN(violation.getBaseVoltage()) || violation.getBaseVoltage() > minBaseVoltage)
                .filter(violation -> countries == null || countries.contains(violation.getCountry()))
                .collect(Collectors.toList());
    }
}
