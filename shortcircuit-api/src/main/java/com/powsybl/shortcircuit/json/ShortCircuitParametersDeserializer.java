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
import com.powsybl.shortcircuit.ShortCircuitParameters;
import com.powsybl.shortcircuit.StudyType;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.shortcircuit.json.JsonShortCircuitParameters.getExtensionSerializers;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersDeserializer extends StdDeserializer<ShortCircuitParameters> {

    private static final String CONTEXT_NAME = "ShortCircuitFaultParameters";

    public ShortCircuitParametersDeserializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new ShortCircuitParameters());
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, ShortCircuitParameters parameters) throws IOException {
        String version = null;
        List<Extension<ShortCircuitParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "withLimitViolations":
                    parser.nextToken();
                    parameters.setWithLimitViolations(parser.readValueAs(Boolean.class));
                    break;
                case "withVoltageMap":
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMap", version, "1.1");
                    parser.nextToken();
                    parameters.setWithVoltageResult(parser.readValueAs(Boolean.class));
                    break;
                case "withVoltageResult":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVoltageProfileResult", version, "1.1");
                    parser.nextToken();
                    parameters.setWithVoltageResult(parser.readValueAs(Boolean.class));
                    break;
                case "withFeederResult":
                    parser.nextToken();
                    parameters.setWithFeederResult(parser.readValueAs(Boolean.class));
                    break;
                case "studyType":
                    parser.nextToken();
                    parameters.setStudyType(JsonUtil.readValue(deserializationContext, parser, StudyType.class));
                    break;
                case "minVoltageDropProportionalThreshold":
                    parser.nextToken();
                    parameters.setMinVoltageDropProportionalThreshold(parser.readValueAs(Double.class));
                    break;
                case "withFortescueResult":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVoltageDropProfileResult", version, "1.1");
                    parser.nextToken();
                    parameters.setWithFortescueResult(parser.readValueAs(Boolean.class));
                    break;
                case "subTransientCoefficient":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: subtransientCoefficient", version, "1.2");
                    parser.nextToken();
                    parameters.setSubTransientCoefficient(parser.readValueAs(Double.class));
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
