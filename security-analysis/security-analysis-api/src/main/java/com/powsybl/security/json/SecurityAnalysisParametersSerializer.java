/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.security.SecurityAnalysisParameters;

import java.io.IOException;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisParametersSerializer extends StdSerializer<SecurityAnalysisParameters> {

    SecurityAnalysisParametersSerializer() {
        super(SecurityAnalysisParameters.class);
    }

    @Override
    public void serialize(SecurityAnalysisParameters parameters, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("version", SecurityAnalysisParameters.VERSION);
        serializerProvider.defaultSerializeField("increased-violations-parameters", parameters.getIncreasedViolationsParameters(), jsonGenerator);
        jsonGenerator.writeBooleanField("intermediate-results-in-operator-strategy", parameters.getIntermediateResultsInOperatorStrategy());
        jsonGenerator.writeFieldName("load-flow-parameters");
        JsonLoadFlowParameters.serialize(parameters.getLoadFlowParameters(), jsonGenerator, serializerProvider);
        JsonUtil.writeExtensions(parameters, jsonGenerator, serializerProvider, JsonSecurityAnalysisParameters.getExtensionSerializers()::get);
        jsonGenerator.writeEndObject();
    }
}
