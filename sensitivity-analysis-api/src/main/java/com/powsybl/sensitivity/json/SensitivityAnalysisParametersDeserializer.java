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
import com.powsybl.sensitivity.SensitivityOperatorStrategiesCalculationMode;

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

    private class ParsingContext {
        String version = null;
        Double flowFlowSensitivityValueThreshold = null;
        Double voltageVoltageSensitivityValueThreshold = null;
        Double flowVoltageSensitivityValueThreshold = null;
        Double angleFlowSensitivityValueThreshold = null;
        SensitivityOperatorStrategiesCalculationMode operatorStrategiesCalculationMode = null;
    }

    @Override
    public SensitivityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new SensitivityAnalysisParameters());
    }

    @Override
    public SensitivityAnalysisParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, SensitivityAnalysisParameters parameters) throws IOException {
        ParsingContext context = new ParsingContext();
        List<Extension<SensitivityAnalysisParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {

                case "version":
                    parser.nextToken();
                    context.version = parser.getValueAsString();
                    break;

                case "load-flow-parameters":
                    parser.nextToken();
                    JsonLoadFlowParameters.deserialize(parser, deserializationContext, parameters.getLoadFlowParameters());
                    break;

                case "flow-flow-sensitivity-value-threshold":
                    parser.nextToken();
                    context.flowFlowSensitivityValueThreshold = parser.readValueAs(Double.class);
                    break;

                case "voltage-voltage-sensitivity-value-threshold":
                    parser.nextToken();
                    context.voltageVoltageSensitivityValueThreshold = parser.readValueAs(Double.class);
                    break;

                case "flow-voltage-sensitivity-value-threshold":
                    parser.nextToken();
                    context.flowVoltageSensitivityValueThreshold = parser.readValueAs(Double.class);
                    break;

                case "angle-flow-sensitivity-value-threshold":
                    parser.nextToken();
                    context.angleFlowSensitivityValueThreshold = parser.readValueAs(Double.class);
                    break;

                case "operator-strategies-calculation-mode":
                    parser.nextToken();
                    context.operatorStrategiesCalculationMode = SensitivityOperatorStrategiesCalculationMode.valueOf(parser.getValueAsString());
                    break;

                case "extensions":
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, JsonSensitivityAnalysisParameters.getExtensionSerializers()::get, parameters);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        checkAndFillVersionedParameters(parameters, context);

        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        return parameters;
    }

    private static void checkAndFillVersionedParameters(SensitivityAnalysisParameters parameters, ParsingContext context) {
        String version = context.version;
        if (context.flowFlowSensitivityValueThreshold != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "flow-flow-sensitivity-value-threshold",
                    version, "1.1");
            parameters.setFlowVoltageSensitivityValueThreshold(context.flowFlowSensitivityValueThreshold);
        }
        if (context.voltageVoltageSensitivityValueThreshold != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "voltage-voltage-sensitivity-value-threshold",
                    version, "1.1");
            parameters.setVoltageVoltageSensitivityValueThreshold(context.voltageVoltageSensitivityValueThreshold);
        }
        if (context.flowVoltageSensitivityValueThreshold != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "flow-voltage-sensitivity-value-threshold",
                    version, "1.1");
            parameters.setFlowVoltageSensitivityValueThreshold(context.flowVoltageSensitivityValueThreshold);
        }
        if (context.angleFlowSensitivityValueThreshold != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "angle-flow-sensitivity-value-threshold",
                    version, "1.1");
            parameters.setAngleFlowSensitivityValueThreshold(context.angleFlowSensitivityValueThreshold);
        }
        if (context.operatorStrategiesCalculationMode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "operator-strategies-calculation-mode",
                    version, "1.2");
            parameters.setOperatorStrategiesCalculationMode(context.operatorStrategiesCalculationMode);
        }
    }
}
