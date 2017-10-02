/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import com.powsybl.iidm.network.Country;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class CIM1ConverterConfig {

    private final boolean invertVoltageStepIncrementOutOfPhase;

    private final Country defaultCountry;

    private final CIM1NamingStrategyFactory namingStrategyFactory;

    private final List<String> substationIdExcludedFromMapping;

    CIM1ConverterConfig(boolean invertVoltageStepIncrementOutOfPhase, Country defaultCountry,
                        List<String> substationIdExcludedFromMapping, CIM1NamingStrategyFactory namingStrategyFactory) {
        this.invertVoltageStepIncrementOutOfPhase = invertVoltageStepIncrementOutOfPhase;
        this.defaultCountry = Objects.requireNonNull(defaultCountry);
        this.substationIdExcludedFromMapping = Objects.requireNonNull(substationIdExcludedFromMapping);
        this.namingStrategyFactory = Objects.requireNonNull(namingStrategyFactory);
    }

    boolean isInvertVoltageStepIncrementOutOfPhase() {
        return invertVoltageStepIncrementOutOfPhase;
    }

    Country getDefaultCountry() {
        return defaultCountry;
    }

    public List<String> getSubstationIdExcludedFromMapping() {
        return substationIdExcludedFromMapping;
    }

    CIM1NamingStrategyFactory getNamingStrategyFactory() {
        return namingStrategyFactory;
    }
}
