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
import com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.security.dynamic.json.JsonDynamicSecurityAnalysisParameters.getExtensionSerializers;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisParametersDeserializer extends StdDeserializer<DynamicSecurityAnalysisParameters> {

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
            switch (parser.currentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "dynamic-simulation-parameters":
                    parser.nextToken();
                    JsonDynamicSimulationParameters.deserialize(parser, deserializationContext, parameters.getDynamicSimulationParameters());
                    break;
                case "contingencies-parameters":
                    parser.nextToken();
                    parameters.setDynamicContingenciesParameters(JsonUtil.readValue(deserializationContext,
                            parser,
                            DynamicSecurityAnalysisParameters.ContingenciesParameters.class));
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
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
