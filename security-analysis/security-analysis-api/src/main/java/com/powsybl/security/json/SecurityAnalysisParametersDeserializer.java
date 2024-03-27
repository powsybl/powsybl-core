/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.security.SecurityAnalysisParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.security.json.JsonSecurityAnalysisParameters.getExtensionSerializers;

/**
 * @author Sylvain Leclerc {@literal <sylvain.leclerc at rte-france.com>}
 */
public class SecurityAnalysisParametersDeserializer extends StdDeserializer<SecurityAnalysisParameters> {

    private static final String CONTEXT_NAME = "SecurityAnalysisParameters";

    SecurityAnalysisParametersDeserializer() {
        super(SecurityAnalysisParameters.class);
    }

    @Override
    public SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new SecurityAnalysisParameters());
    }

    @Override
    public SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, SecurityAnalysisParameters parameters) throws IOException {
        List<Extension<SecurityAnalysisParameters>> extensions = Collections.emptyList();
        String version = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "increased-violations-parameters":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, "1.0");
                    parser.nextToken();
                    parameters.setIncreasedViolationsParameters(JsonUtil.readValue(deserializationContext,
                            parser,
                            SecurityAnalysisParameters.IncreasedViolationsParameters.class));
                    break;
                case "load-flow-parameters":
                    parser.nextToken();
                    JsonLoadFlowParameters.deserialize(parser, deserializationContext, parameters.getLoadFlowParameters());
                    break;
                case "intermediate-results-in-operator-strategy":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, "1.2");
                    parser.nextToken();
                    parameters.setIntermediateResultsInOperatorStrategy(parser.getValueAsBoolean());
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }
}
