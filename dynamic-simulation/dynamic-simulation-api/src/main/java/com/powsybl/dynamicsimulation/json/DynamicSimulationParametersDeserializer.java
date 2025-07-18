/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.dynamicsimulation.json;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.dynamicsimulation.DynamicSimulationParameters;

import static com.powsybl.dynamicsimulation.json.JsonDynamicSimulationParameters.getExtensionSerializers;

/**
 * @author Marcos de Miguel {@literal <demiguelm at aia.es>}
 */
public class DynamicSimulationParametersDeserializer extends StdDeserializer<DynamicSimulationParameters> {

    private static final String CONTEXT_NAME = "DynamicSimulationParameters";
    private static final String TAG = "Tag: ";

    DynamicSimulationParametersDeserializer() {
        super(DynamicSimulationParameters.class);
    }

    @Override
    public DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new DynamicSimulationParameters());
    }

    @Override
    public DynamicSimulationParameters deserialize(JsonParser parser, DeserializationContext deserializationContext,
                                                   DynamicSimulationParameters parameters) throws IOException {
        String version = null;
        List<Extension<DynamicSimulationParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {

                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;

                case "startTime":
                    parser.nextToken();
                    parameters.setStartTime(parser.readValueAs(Integer.class));
                    break;

                case "stopTime":
                    parser.nextToken();
                    parameters.setStopTime(parser.readValueAs(Integer.class));
                    break;

                case "debugDir":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.currentName(), version, "1.1");
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
