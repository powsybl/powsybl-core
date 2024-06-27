/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;

import java.io.IOException;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParametersSerializer extends StdSerializer<DynamicSecurityAnalysisParameters> {

    DynamicSecurityAnalysisParametersSerializer() {
        super(DynamicSecurityAnalysisParameters.class);
    }

    @Override
    public void serialize(DynamicSecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", DynamicSecurityAnalysisParameters.VERSION);
        jsonGenerator.writeFieldName("dynamic-simulation-parameters");
        JsonDynamicSimulationParameters.serialize(parameters.getDynamicSimulationParameters(), jsonGenerator, serializerProvider);
        serializerProvider.defaultSerializeField("contingencies-parameters", parameters.getDynamicContingenciesParameters(), jsonGenerator);
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonDynamicSecurityAnalysisParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }
}
