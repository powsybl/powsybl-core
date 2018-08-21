/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.sensitivity.SensitivityComputationParameters;

import java.io.IOException;

/**
 * Json serializer for sensitivity computation parameters
 *
 * @author Sebastien Murgey <sebastien.murgey at rte-france.com>
 */
public class SensitivityComputationParametersSerializer extends StdSerializer<SensitivityComputationParameters> {

    SensitivityComputationParametersSerializer() {
        super(SensitivityComputationParameters.class);
    }

    @Override
    public void serialize(SensitivityComputationParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {

        jsonGenerator.writeStartObject();

        jsonGenerator.writeStringField("version", SensitivityComputationParameters.VERSION);

        jsonGenerator.writeFieldName("load-flow-parameters");
        JsonLoadFlowParameters.serialize(parameters.getLoadFlowParameters(), jsonGenerator, serializerProvider);

        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonSensitivityComputationParameters.getExtensionSerializers());

        jsonGenerator.writeEndObject();
    }
}
