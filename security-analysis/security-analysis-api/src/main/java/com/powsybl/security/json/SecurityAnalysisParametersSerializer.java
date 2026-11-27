/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.security.SecurityAnalysisParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

import java.util.Set;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisParametersSerializer extends StdSerializer<SecurityAnalysisParameters> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAnalysisParametersSerializer.class);

    SecurityAnalysisParametersSerializer() {
        super(SecurityAnalysisParameters.class);
    }

    @Override
    public void serialize(SecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", SecurityAnalysisParameters.VERSION);
        serializationContext.defaultSerializeProperty("increased-violations-parameters", parameters.getIncreasedViolationsParameters(), jsonGenerator);
        serializationContext.defaultSerializeProperty("monitored-element-modification-threshold", parameters.getModifiedMonitoredElementsParameters(), jsonGenerator);
        jsonGenerator.writeBooleanProperty("intermediate-results-in-operator-strategy", parameters.getIntermediateResultsInOperatorStrategy());
        JsonUtil.writeOptionalStringProperty(jsonGenerator, "debug-dir", parameters.getDebugDir());
        jsonGenerator.writeName("load-flow-parameters");
        JsonLoadFlowParameters.serialize(parameters.getLoadFlowParameters(), jsonGenerator, serializationContext);
        Set<String> notSerialized = JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonSecurityAnalysisParameters.getExtensionSerializers()::get);
        if (!notSerialized.isEmpty()) {
            LOGGER.warn("No JSON serializer found for security analysis parameters extensions {}, they are not written", notSerialized);
        }
        jsonGenerator.writeEndObject();
    }
}
