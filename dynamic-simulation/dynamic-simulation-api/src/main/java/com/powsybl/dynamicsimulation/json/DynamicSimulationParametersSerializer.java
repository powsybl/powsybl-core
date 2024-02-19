/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dynamicsimulation.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;

import java.io.IOException;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSimulationParametersSerializer extends AbstractDynamicSimulationParametersSerializer<DynamicSimulationParameters> {

    DynamicSimulationParametersSerializer() {
        super(DynamicSimulationParameters.class);
    }

    @Override
    public void serialize(DynamicSimulationParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        serializeCommon(parameters, jsonGenerator);
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonDynamicSimulationParameters.getExtensionSerializers());
        jsonGenerator.writeEndObject();
    }
}
