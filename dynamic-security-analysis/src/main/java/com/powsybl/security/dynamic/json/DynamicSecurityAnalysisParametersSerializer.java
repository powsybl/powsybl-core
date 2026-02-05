/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParametersSerializer extends StdSerializer<DynamicSecurityAnalysisParameters> {

    DynamicSecurityAnalysisParametersSerializer() {
        super(DynamicSecurityAnalysisParameters.class);
    }

    @Override
    public void serialize(DynamicSecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", DynamicSecurityAnalysisParameters.VERSION);
        jsonGenerator.writeName("dynamic-simulation-parameters");
        JsonDynamicSimulationParameters.serialize(parameters.getDynamicSimulationParameters(), jsonGenerator, serializationContext);
        serializationContext.defaultSerializeProperty("contingencies-parameters", parameters.getDynamicContingenciesParameters(), jsonGenerator);
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debugDir", parameters.getDebugDir());
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonDynamicSecurityAnalysisParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }
}
