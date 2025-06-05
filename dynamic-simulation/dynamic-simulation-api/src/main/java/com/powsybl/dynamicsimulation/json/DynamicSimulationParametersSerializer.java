/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationParametersSerializer extends StdSerializer<DynamicSimulationParameters> {

    DynamicSimulationParametersSerializer() {
        super(DynamicSimulationParameters.class);
    }

    @Override
    public void serialize(DynamicSimulationParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", DynamicSimulationParameters.VERSION);
        jsonGenerator.writeNumberField("startTime", parameters.getStartTime());
        jsonGenerator.writeNumberField("stopTime", parameters.getStopTime());
        jsonGenerator.writeStringField("debugDir", parameters.getDebugDir());

        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonDynamicSimulationParameters.getExtensionSerializers());

        jsonGenerator.writeEndObject();
    }
}
