/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.powsybl.shortcircuit.json.JsonShortCircuitParameters.getExtensionSerializers;
import static com.powsybl.shortcircuit.json.ParametersDeserializationConstants.*;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParametersDeserializer extends StdDeserializer<ShortCircuitParameters> {

    private static final String CONTEXT_NAME = "ShortCircuitFaultParameters";
    private static final String TAG = "Tag: ";

    private class ParsingContext {
        String version = null;
        Boolean withVoltageResult = null;
        Boolean withVoltageMap = null;
        Boolean withFortescueResult = null;
        Double subTransientCoefficient = null;
        Boolean withLoads = null;
        Boolean withShuntCompensators = null;
        Boolean withVSCConverterStations = null;
        Boolean withNeutralPosition = null;
        InitialVoltageProfileMode initialVoltageProfileMode = null;
        JsonNode voltageRanges = null;
        Boolean detailedReport = null;
        String debugDir = null;
    }

    public ShortCircuitParametersDeserializer() {
        super(ShortCircuitParameters.class);
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        return deserialize(parser, deserializationContext, new ShortCircuitParameters());
    }

    @Override
    public ShortCircuitParameters deserialize(JsonParser parser, DeserializationContext deserializationContext, ShortCircuitParameters parameters) throws IOException {
        ParsingContext context = new ParsingContext();
        List<Extension<ShortCircuitParameters>> extensions = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> {
                    parser.nextToken();
                    context.version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(deserializationContext, context.version, SOURCE_VERSION_ATTRIBUTE);
                    deserializationContext.setAttribute(SOURCE_PARAMETER_TYPE_ATTRIBUTE, ParametersType.SHORT_CIRCUIT);
                }
                case "withLimitViolations" -> {
                    parser.nextToken();
                    parameters.setWithLimitViolations(parser.readValueAs(Boolean.class));
                }
                case "withVoltageMap" -> {
                    parser.nextToken();
                    context.withVoltageMap = parser.readValueAs(Boolean.class);
                }
                case "withVoltageResult" -> {
                    parser.nextToken();
                    context.withVoltageResult = parser.readValueAs(Boolean.class);
                }
                case "withFeederResult" -> {
                    parser.nextToken();
                    parameters.setWithFeederResult(parser.readValueAs(Boolean.class));
                }
                case "studyType" -> {
                    parser.nextToken();
                    parameters.setStudyType(JsonUtil.readValue(deserializationContext, parser, StudyType.class));
                }
                case "minVoltageDropProportionalThreshold" -> {
                    parser.nextToken();
                    parameters.setMinVoltageDropProportionalThreshold(parser.readValueAs(Double.class));
                }
                case "withFortescueResult" -> {
                    parser.nextToken();
                    context.withFortescueResult = parser.readValueAs(Boolean.class);
                }
                case "subTransientCoefficient" -> {
                    parser.nextToken();
                    context.subTransientCoefficient = parser.readValueAs(Double.class);
                }
                case "withLoads" -> {
                    parser.nextToken();
                    context.withLoads = parser.readValueAs(Boolean.class);
                }
                case "withShuntCompensators" -> {
                    parser.nextToken();
                    context.withShuntCompensators = parser.readValueAs(Boolean.class);
                }
                case "withVSCConverterStations" -> {
                    parser.nextToken();
                    context.withVSCConverterStations = parser.readValueAs(Boolean.class);
                }
                case "withNeutralPosition" -> {
                    parser.nextToken();
                    context.withNeutralPosition = parser.readValueAs(Boolean.class);
                }
                case "initialVoltageProfileMode" -> {
                    parser.nextToken();
                    context.initialVoltageProfileMode = JsonUtil.readValue(deserializationContext, parser, InitialVoltageProfileMode.class);
                }
                case "voltageRanges" -> {
                    parser.nextToken();
                    context.voltageRanges = parser.readValueAsTree();
                }
                case "detailedReport" -> {
                    parser.nextToken();
                    context.detailedReport = parser.readValueAs(Boolean.class);
                }
                case "debugDir" -> {
                    parser.nextToken();
                    context.debugDir = parser.readValueAs(String.class);
                }
                case "extensions" -> {
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        String version = context.version;
        if (context.withVoltageMap != null) {
            JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, TAG + "withVoltageMap", version, "1.1");
            parameters.setWithVoltageResult(context.withVoltageMap);
        }
        if (context.withVoltageResult != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withVoltageResult", version, "1.1");
            parameters.setWithVoltageResult(context.withVoltageResult);
        }
        if (context.withFortescueResult != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withFortescueResult", version, "1.2");
            parameters.setWithFortescueResult(context.withFortescueResult);
        }
        if (context.subTransientCoefficient != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "subTransientCoefficient", version, "1.2");
            parameters.setSubTransientCoefficient(context.subTransientCoefficient);
        }
        if (context.withLoads != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withLoads", version, "1.2");
            parameters.setWithLoads(context.withLoads);
        }
        if (context.withShuntCompensators != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withShuntCompensators", version, "1.2");
            parameters.setWithShuntCompensators(context.withShuntCompensators);
        }
        if (context.withVSCConverterStations != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withVSCConverterStations", version, "1.2");
            parameters.setWithVSCConverterStations(context.withVSCConverterStations);
        }
        if (context.withNeutralPosition != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "withNeutralPosition", version, "1.2");
            parameters.setWithNeutralPosition(context.withNeutralPosition);
        }
        if (context.initialVoltageProfileMode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "initialVoltageProfileMode", version, "1.2");
            parameters.setInitialVoltageProfileMode(context.initialVoltageProfileMode);
        }
        if (context.voltageRanges != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "voltageRanges", version, "1.2");
            parameters.setVoltageRanges(JsonUtil.readFromNode(context.voltageRanges, deserializationContext, VoltageRange.class, parser.getCodec()));
        }
        if (context.detailedReport != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "detailedReport", version, "1.3");
            parameters.setDetailedReport(context.detailedReport);
        }
        if (context.debugDir != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + "debugDir", version, "1.4");
            parameters.setDebugDir(context.debugDir);
        }

        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        parameters.validate();
        return parameters;
    }

}
