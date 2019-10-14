/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamic.simulation.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.dynamic.simulation.DynamicSimulationParameters;

/**
 * @author Marcos de Miguel <demiguelm at aia.es>
 */
public class DynamicSimulationParametersJsonModule extends SimpleModule {

    public DynamicSimulationParametersJsonModule() {
        addDeserializer(DynamicSimulationParameters.class, new DynamicSimulationParametersDeserializer());
        addSerializer(DynamicSimulationParameters.class, new DynamicSimulationParametersSerializer());
    }
}
