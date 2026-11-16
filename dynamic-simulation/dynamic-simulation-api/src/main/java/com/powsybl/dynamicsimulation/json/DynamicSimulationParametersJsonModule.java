/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationParametersJsonModule extends SimpleModule {

    public DynamicSimulationParametersJsonModule() {
        addDeserializer(DynamicSimulationParameters.class, new DynamicSimulationParametersDeserializer());
        addSerializer(DynamicSimulationParameters.class, new DynamicSimulationParametersSerializer());
    }
}
