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
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisParametersSerializer extends StdSerializer<SecurityAnalysisParameters> {

    SecurityAnalysisParametersSerializer() {
        super(SecurityAnalysisParameters.class);
    }

    @Override
    public void serialize(SecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializationContext serializationContext) throws JacksonException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringProperty("version", SecurityAnalysisParameters.VERSION);
        serializationContext.defaultSerializeProperty("increased-violations-parameters", parameters.getIncreasedViolationsParameters(), jsonGenerator);
        jsonGenerator.writeBooleanProperty("intermediate-results-in-operator-strategy", parameters.getIntermediateResultsInOperatorStrategy());
        jsonGenerator.writeName("load-flow-parameters");
        JsonLoadFlowParameters.serialize(parameters.getLoadFlowParameters(), jsonGenerator, serializationContext);
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializationContext, JsonSecurityAnalysisParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }
}
