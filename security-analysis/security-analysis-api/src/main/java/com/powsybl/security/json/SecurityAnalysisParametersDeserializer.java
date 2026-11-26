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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
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
public class SecurityAnalysisParametersDeserializer extends StdDeserializer<SecurityAnalysisParameters> implements ContextualDeserializer {

    private static final String CONTEXT_NAME = "SecurityAnalysisParameters";
    private static final String TAG = "Tag: ";

    private final transient JsonDeserializer<Object> increasedViolationsParametersDeserializer;
    private final transient JsonDeserializer<Object> modifiedElementsParametersDeserializer;

    SecurityAnalysisParametersDeserializer() {
        this(null, null);
    }

    SecurityAnalysisParametersDeserializer(JsonDeserializer<Object> increasedViolationsParametersDeserializer,
                                           JsonDeserializer<Object> modifiedElementsParametersDeserializer) {
        super(SecurityAnalysisParameters.class);
        this.increasedViolationsParametersDeserializer = increasedViolationsParametersDeserializer;
        this.modifiedElementsParametersDeserializer = modifiedElementsParametersDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new SecurityAnalysisParametersDeserializer(
            JsonUtil.buildValueDeserializer(ctxt, property, SecurityAnalysisParameters.IncreasedViolationsParameters.class),
            JsonUtil.buildValueDeserializer(ctxt, property, SecurityAnalysisParameters.ModifiedMonitoredElementsParameters.class)
        );
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
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "increased-violations-parameters":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, "1.0");
                    parser.nextToken();
                    parameters.setIncreasedViolationsParameters(JsonUtil.readValue(
                            increasedViolationsParametersDeserializer,
                            deserializationContext,
                            parser,
                            SecurityAnalysisParameters.IncreasedViolationsParameters.class));
                    break;
                case "monitored-element-modification-threshold":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: monitoredElementModificationThreshold", version, "1.3");
                    parser.nextToken();
                    parameters.setModifiedMonitoredElementsParameters(JsonUtil.readValue(
                            modifiedElementsParametersDeserializer,
                            deserializationContext,
                            parser,
                            SecurityAnalysisParameters.ModifiedMonitoredElementsParameters.class));
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
                case "debug-dir":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.currentName(), version, "1.3");
                    parser.nextToken();
                    parameters.setDebugDir(parser.readValueAs(String.class));
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }
}
