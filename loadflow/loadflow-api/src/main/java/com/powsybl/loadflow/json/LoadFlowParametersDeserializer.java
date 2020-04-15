/**
 * Copyright (c) 2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowParameters.VoltageInitMode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class LoadFlowParametersDeserializer extends StdDeserializer<LoadFlowParameters> {

    static final String CONTEXT_NAME = "LoadFlowParameters";
    static final String REFERENCE_VERSION = "1.0";

    LoadFlowParametersDeserializer() {
        super(LoadFlowParameters.class);
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new LoadFlowParameters());
    }

    @Override
    public LoadFlowParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, LoadFlowParameters parameters) throws IOException {
        String version = null;
        List<Extension<LoadFlowParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;

                case "voltageInitMode":
                    parser.nextToken();
                    parameters.setVoltageInitMode(parser.readValueAs(VoltageInitMode.class));
                    break;

                case "transformerVoltageControlOn":
                    parser.nextToken();
                    parameters.setTransformerVoltageControlOn(parser.readValueAs(Boolean.class));
                    break;

                case "noGeneratorReactiveLimits":
                    parser.nextToken();
                    parameters.setNoGeneratorReactiveLimits(parser.readValueAs(Boolean.class));
                    break;

                case "phaseShifterRegulationOn":
                    parser.nextToken();
                    parameters.setPhaseShifterRegulationOn(parser.readValueAs(Boolean.class));
                    break;

                case "specificCompatibility":
                    JsonUtil.assertLessThanOrEqualToReferenceVersion(CONTEXT_NAME, "Tag: specificCompatibility", version, REFERENCE_VERSION);
                    parser.nextToken();
                    parameters.setT2wtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "t2wtSplitShuntAdmittance":
                    JsonUtil.assertGreaterThanReferenceVersion(CONTEXT_NAME, "Tag: t2wtSplitShuntAdmittance", version, REFERENCE_VERSION);
                    parser.nextToken();
                    parameters.setT2wtSplitShuntAdmittance(parser.readValueAs(Boolean.class));
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, JsonLoadFlowParameters.getExtensionSerializers(), parameters);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        JsonLoadFlowParameters.getExtensionSerializers().addExtensions(parameters, extensions);
        return parameters;
    }
}
