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

    private class ParsingContext {
        String version;
        SecurityAnalysisParameters.IncreasedViolationsParameters increasedViolationsParameters = null;
        Boolean intermediateResultsInOperatorStrategy = null;
    }

    @Override
    public SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new SecurityAnalysisParameters());
    }

    @Override
    public SecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, SecurityAnalysisParameters parameters) throws IOException {
        ParsingContext context = new ParsingContext();
        List<Extension<SecurityAnalysisParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken();
                    context.version = parser.getValueAsString();
                    break;
                case "increased-violations-parameters":
                    parser.nextToken();
                    context.increasedViolationsParameters = JsonUtil.readValue(deserializationContext,
                            parser,
                            SecurityAnalysisParameters.IncreasedViolationsParameters.class);
                    break;
                case "load-flow-parameters":
                    parser.nextToken();
                    JsonLoadFlowParameters.deserialize(parser, deserializationContext, parameters.getLoadFlowParameters());
                    break;
                case "intermediate-results-in-operator-strategy":
                    parser.nextToken();
                    context.intermediateResultsInOperatorStrategy = parser.getValueAsBoolean();
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        checkAndFillVersionedParameters(parameters, context);

        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }

    private static void checkAndFillVersionedParameters(SecurityAnalysisParameters parameters, ParsingContext context) {
        if (context.increasedViolationsParameters != null) {
            JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", context.version, "1.0");
            parameters.setIncreasedViolationsParameters(context.increasedViolationsParameters);
        }
        if (context.intermediateResultsInOperatorStrategy != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", context.version, "1.2");
            parameters.setIntermediateResultsInOperatorStrategy(context.intermediateResultsInOperatorStrategy);
        }
    }
}
