/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.ShortCircuitConstants;
import com.powsybl.shortcircuit.ShortCircuitParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersDeserializer extends StdDeserializer<ShortCircuitParameters> {

    public ShortCircuitParametersDeserializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new ShortCircuitParameters());
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, ShortCircuitParameters parameters) throws IOException {
        List<Extension<ShortCircuitParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); // skip
                    break;
                case "withFeederResult":
                    parser.nextToken();
                    parameters.setWithFeederResult(parser.readValueAs(Boolean.class));
                    break;
                case "studyType":
                    parser.nextToken();
                    parameters.setStudyType(parser.readValueAs(ShortCircuitConstants.StudyType.class));
                    break;
                case "minVoltageDropProportionalThreshold":
                    parser.nextToken();
                    parameters.setMinVoltageDropProportionalThreshold(parser.readValueAs(Double.class));
                    break;
                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, JsonShortCircuitParameters.getExtensionSerializers(), parameters);
                    break;
                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        JsonShortCircuitParameters.getExtensionSerializers().addExtensions(parameters, extensions);
        return parameters;
    }

}
