/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilter {

    private static final Set<LimitViolationType> DEFAULT_VIOLATION_TYPES = EnumSet.allOf(LimitViolationType.class);
    private static final double DEFAULT_MIN_BASE_VOLTAGE = 0.0;
    private static final Set<Country> DEFAULT_COUNTRIES = EnumSet.allOf(Country.class);

    private static Set<LimitViolationType> checkViolationTypes(Set<LimitViolationType> violationTypes) {
        if (violationTypes == null) {
            return DEFAULT_VIOLATION_TYPES;
        } else if (!violationTypes.isEmpty()) {
            return violationTypes;
        } else {
            throw new IllegalArgumentException("Bad violation types filter");
        }
    }

    private static double checkMinBaseVoltage(double minBaseVoltage) {
        if (Double.isNaN(minBaseVoltage) || minBaseVoltage < 0) {
            throw new IllegalArgumentException("Bad min base voltage filter " + minBaseVoltage);
        }
        return minBaseVoltage;
    }

    private static Set<Country> checkCountries(Set<Country> countries) {
        if (countries == null) {
            return DEFAULT_COUNTRIES;
        } else if (!countries.isEmpty()) {
            return countries;
        } else {
            throw new IllegalArgumentException("Bad countries filter");
        }
    }

    public static LimitViolationFilter load() {
        return load(PlatformConfig.defaultConfig());
    }

    static LimitViolationFilter load(PlatformConfig platformConfig) {
        LimitViolationFilter filter = new LimitViolationFilter();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("limit-violation-default-filter");
        if (moduleConfig != null) {
            filter.setViolationTypes(moduleConfig.getEnumSetProperty("violationTypes", LimitViolationType.class, DEFAULT_VIOLATION_TYPES));
            filter.setMinBaseVoltage(moduleConfig.getDoubleProperty("minBaseVoltage", DEFAULT_MIN_BASE_VOLTAGE));
            filter.setCountries(moduleConfig.getEnumSetProperty("countries", Country.class, DEFAULT_COUNTRIES));
        }
        return filter;
    }

    private Set<LimitViolationType> violationTypes;

    private double minBaseVoltage;

    private Set<Country> countries;

    public LimitViolationFilter(Set<LimitViolationType> violationTypes) {
        this(violationTypes, DEFAULT_MIN_BASE_VOLTAGE, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter() {
        this(DEFAULT_VIOLATION_TYPES, DEFAULT_MIN_BASE_VOLTAGE, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, double minBaseVoltage) {
        this(violationTypes, minBaseVoltage, DEFAULT_COUNTRIES);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, double minBaseVoltage, Set<Country> countries) {
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

    public double getMinBaseVoltage() {
        return minBaseVoltage;
    }

    public LimitViolationFilter setMinBaseVoltage(double minBaseVoltage) {
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

    public List<LimitViolation> apply(List<LimitViolation> violations, Network network) {
        Objects.requireNonNull(violations);
        Objects.requireNonNull(network);

        return violations.stream()
            .filter(violation -> accept(violation.getLimitType()))
            .filter(violation -> accept(LimitViolationHelper.getNominalVoltage(violation, network)))
            .filter(violation -> accept(LimitViolationHelper.getCountry(violation, network)))
            .collect(Collectors.toList());
    }

    private boolean accept(Country country) {
        return (country == null) || countries.contains(country);
    }

    private boolean accept(double baseVoltage) {
        return Double.isNaN(baseVoltage) || baseVoltage >= minBaseVoltage;
    }

    private boolean accept(LimitViolationType limitViolationType) {
        return violationTypes.contains(limitViolationType);
    }
}
