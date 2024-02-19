/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.json.AbstractDynamicSimulationParametersSerializer;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.security.dynamic.DynamicSimulationContingenciesParameters;

import java.io.IOException;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationContingenciesParametersSerializer extends AbstractDynamicSimulationParametersSerializer<DynamicSimulationContingenciesParameters> {

    DynamicSimulationContingenciesParametersSerializer() {
        super(DynamicSimulationContingenciesParameters.class);
    }

    @Override
    public void serialize(DynamicSimulationContingenciesParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializeCommon(parameters, jsonGenerator);
        jsonGenerator.writeNumberField("contingenciesStartTime", parameters.getContingenciesStartTime());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonDynamicSimulationParameters.getExtensionSerializers());
        jsonGenerator.writeEndObject();
    }
}
