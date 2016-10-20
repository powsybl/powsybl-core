/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.security.LimitViolation;
import eu.itesla_project.security.LimitViolationType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifierConfig {

    public static final Set<LimitViolationType> DEFAULT_VIOLATION_TYPES = EnumSet.of(LimitViolationType.CURRENT);
    public static final Set<Country> DEFAULT_COUNTRIES = Collections.emptySet();

    private final Set<Country> countries;
    private final Set<LimitViolationType> violationsTypes;

    public static ConstraintsModifierConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ConstraintsModifierConfig load(PlatformConfig platformConfig) {
        Set<LimitViolationType> violationsTypes;
        Set<Country> countries;
        if (platformConfig.moduleExists("constraintsModifier")) {
            ModuleConfig config = platformConfig.getModuleConfig("constraintsModifier");
            violationsTypes = config.getEnumSetProperty("violationsTypes", LimitViolationType.class, DEFAULT_VIOLATION_TYPES);
            countries = config.getEnumSetProperty("countries", Country.class, DEFAULT_COUNTRIES);
        } else {
            violationsTypes = DEFAULT_VIOLATION_TYPES;
            countries = DEFAULT_COUNTRIES;
        }
        return new ConstraintsModifierConfig(countries, violationsTypes);
    }

    public ConstraintsModifierConfig(Set<Country> countries, Set<LimitViolationType> violationsTypes) {
        this.countries = countries;
        this.violationsTypes = violationsTypes;
    }

    public Set<Country> getCountries() {
        return countries;
    }

    public Set<LimitViolationType> getViolationsTypes() {
        return violationsTypes;
    }

    public boolean isInAreaOfInterest(LimitViolation violation, Network network) {
        return (countries.isEmpty() || countries.contains(violation.getCountry()));
    }

    @Override
    public String toString() {
        return "ConstraintsModifierConfig[country="+countries+",violation types="+violationsTypes.toString()+"]";
    }

}
