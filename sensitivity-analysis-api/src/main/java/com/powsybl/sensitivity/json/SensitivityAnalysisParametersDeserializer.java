/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.sensitivity.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.loadflow.json.JsonLoadFlowParameters;
import com.powsybl.sensitivity.SensitivityAnalysisParameters;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Json deserializer for sensitivity analysis parameters
 *
 * @author Sebastien Murgey {@literal <sebastien.murgey at rte-france.com>}
 */
public class SensitivityAnalysisParametersDeserializer extends StdDeserializer<SensitivityAnalysisParameters> {

    private static final String CONTEXT_NAME = "SensitivityAnalysisParameters";

    SensitivityAnalysisParametersDeserializer() {
        super(SensitivityAnalysisParameters.class);
    }

    @Override
    public SensitivityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new SensitivityAnalysisParameters());
    }

    @Override
    public SensitivityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, SensitivityAnalysisParameters parameters) throws IOException {

        List<Extension<SensitivityAnalysisParameters>> extensions = Collections.emptyList();
        String version = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {

                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;

                case "load-flow-parameters":
                    parser.nextToken();
                    JsonLoadFlowParameters.deserialize(parser, deserializationContext, parameters.getLoadFlowParameters());
                    break;

                case "flow-flow-sensitivity-value-threshold":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, parser.currentName(), version, "1.1");
                    parser.nextToken();
                    parameters.setFlowFlowSensitivityValueThreshold(parser.readValueAs(Double.class));
                    break;

                case "voltage-voltage-sensitivity-value-threshold":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, parser.currentName(), version, "1.1");

                    parser.nextToken();
                    parameters.setVoltageVoltageSensitivityValueThreshold(parser.readValueAs(Double.class));
                    break;

                case "flow-voltage-sensitivity-value-threshold":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, parser.currentName(), version, "1.1");

                    parser.nextToken();
                    parameters.setFlowVoltageSensitivityValueThreshold(parser.readValueAs(Double.class));
                    break;

                case "angle-flow-sensitivity-value-threshold":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, parser.currentName(), version, "1.1");

                    parser.nextToken();
                    parameters.setAngleFlowSensitivityValueThreshold(parser.readValueAs(Double.class));
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, JsonSensitivityAnalysisParameters.getExtensionSerializers()::get, parameters);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }

}
