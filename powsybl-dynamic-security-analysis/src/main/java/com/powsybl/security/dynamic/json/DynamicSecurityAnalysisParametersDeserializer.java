/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.AbstractSecurityAnalysisParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.security.dynamic.json.JsonDynamicSecurityAnalysisParameters.getExtensionSerializers;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParametersDeserializer extends StdDeserializer<DynamicSecurityAnalysisParameters> {

    private static final String CONTEXT_NAME = "DynamicSecurityAnalysisParameters";

    DynamicSecurityAnalysisParametersDeserializer() {
        super(DynamicSecurityAnalysisParameters.class);
    }

    @Override
    public DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new DynamicSecurityAnalysisParameters());
    }

    @Override
    public DynamicSecurityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, DynamicSecurityAnalysisParameters parameters) throws IOException {
        List<Extension<DynamicSecurityAnalysisParameters>> extensions = Collections.emptyList();
        String version = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "increased-violations-parameters":
                    parser.nextToken();
                    parameters.setIncreasedViolationsParameters(JsonUtil.readValue(deserializationContext,
                            parser,
                            AbstractSecurityAnalysisParameters.IncreasedViolationsParameters.class));
                    break;
                case "dynamic-simulation-contingencies-parameters":
                    parser.nextToken();
                    JsonDynamicSimulationContingenciesParameters.deserialize(parser, deserializationContext, parameters.getDynamicSimulationParameters());
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (version == null || !version.equals("1.0")) {
            //Only 1.0 version is supported for now
            throw new IllegalStateException("Version different than 1.0 not supported.");
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }
}
