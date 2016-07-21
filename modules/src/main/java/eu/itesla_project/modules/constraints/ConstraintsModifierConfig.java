/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.constraints;

import java.util.Arrays;
import java.util.List;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.security.LimitViolation;
import eu.itesla_project.modules.security.LimitViolationType;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class ConstraintsModifierConfig {

    public static final List<LimitViolationType> DEFAULT_VIOLATION_TYPES = Arrays.asList(LimitViolationType.CURRENT);
    public static final Country DEFAULT_COUNTRY = null;

    private final Country country;
    private final List<LimitViolationType> violationsTypes;

    public static ConstraintsModifierConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ConstraintsModifierConfig load(PlatformConfig platformConfig) {
        List<LimitViolationType> violationsTypes;
        Country country;
        if (platformConfig.moduleExists("constraintsModifier")) {
            ModuleConfig config = platformConfig.getModuleConfig("constraintsModifier");
            violationsTypes = config.getEnumListProperty("violationsTypes", LimitViolationType.class, DEFAULT_VIOLATION_TYPES);
            String countryStr = config.getStringProperty("country", null);
            country = ( countryStr == null ) ? DEFAULT_COUNTRY : Country.valueOf(countryStr);
        } else {
            violationsTypes = DEFAULT_VIOLATION_TYPES;
            country = DEFAULT_COUNTRY;
        }
        return new ConstraintsModifierConfig(country, violationsTypes);
    }

    public ConstraintsModifierConfig(Country country, List<LimitViolationType> violationsTypes) {
        this.country = country;
        this.violationsTypes = violationsTypes;
    }

    public Country getCountry() {
        return country;
    }

    public List<LimitViolationType> getViolationsTypes() {
        return violationsTypes;
    }

    public boolean isInAreaOfInterest(LimitViolation violation, Network network) {
        if ( country != null )
            return violation.getCountry() == country;
        return true;
    }

    @Override
    public String toString() {
        return "ConstraintsModifierConfig[country="+country+",violation types="+violationsTypes.toString()+"]";
    }

}
