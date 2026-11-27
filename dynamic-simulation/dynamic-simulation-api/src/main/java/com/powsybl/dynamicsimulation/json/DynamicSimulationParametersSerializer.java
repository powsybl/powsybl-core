/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationParametersSerializer extends StdSerializer<DynamicSimulationParameters> {

    DynamicSimulationParametersSerializer() {
        super(DynamicSimulationParameters.class);
    }

    @Override
    public void serialize(DynamicSimulationParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringProperty("version", DynamicSimulationParameters.VERSION);
        jsonGenerator.writeNumberProperty("startTime", parameters.getStartTime());
        jsonGenerator.writeNumberProperty("stopTime", parameters.getStopTime());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debugDir", parameters.getDebugDir());

        JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonDynamicSimulationParameters.getExtensionSerializers()::get);

        jsonGenerator.writeEndObject();
    }
}
