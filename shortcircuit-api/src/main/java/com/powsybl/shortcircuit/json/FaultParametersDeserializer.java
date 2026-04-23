/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.InitialVoltageProfileMode;
import com.powsybl.shortcircuit.StudyType;
import com.powsybl.shortcircuit.VoltageRange;

import java.io.IOException;
import java.util.List;

import static com.powsybl.shortcircuit.json.ParametersDeserializationConstants.*;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class FaultParametersDeserializer extends StdDeserializer<FaultParameters> {

    private static final String CONTEXT_NAME = "ShortCircuitFaultParameters";

    private class ParsingContext {
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
    }

    FaultParametersDeserializer() {
        super(FaultParameters.class);
    }

    @Override
    public FaultParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();

        String version = null;
        String id = null;
        boolean withLimitViolations = false;
        boolean withVoltageAndVoltageDropProfileResult = false;
        boolean withFeederResult = false;
        StudyType type = null;
        double minVoltageDropProportionalThreshold = Double.NaN;
        boolean withFortescueResult = false;
        double subTransientCoefficient = Double.NaN;
        boolean withLoads = false;
        boolean withShuntCompensators = false;
        boolean withVSCConverterStations = false;
        boolean withNeutralPosition = false;
        InitialVoltageProfileMode initialVoltageProfileMode = null;
        List<VoltageRange> voltageRanges = null;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> {
                    parser.nextToken();
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(deserializationContext, version, SOURCE_VERSION_ATTRIBUTE);
                    deserializationContext.setAttribute(SOURCE_PARAMETER_TYPE_ATTRIBUTE, ParametersType.FAULT);
                }
                case "id" -> {
                    parser.nextToken();
                    id = parser.readValueAs(String.class);
                }
                case "withLimitViolations" -> {
                    parser.nextToken();
                    withLimitViolations = parser.readValueAs(Boolean.class);
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
                    withFeederResult = parser.readValueAs(Boolean.class);
                }
                case "studyType" -> {
                    parser.nextToken();
                    type = StudyType.valueOf(parser.readValueAs(String.class));
                }
                case "minVoltageDropProportionalThreshold" -> {
                    parser.nextToken();
                    minVoltageDropProportionalThreshold = parser.readValueAs(Double.class);
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
                    context.initialVoltageProfileMode = InitialVoltageProfileMode.valueOf(parser.readValueAs(String.class));
                }
                case "voltageRanges" -> {
                    parser.nextToken();
                    context.voltageRanges = parser.readValueAsTree();
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        if (context.withVoltageMap != null) {
            JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMap", version, "1.1");
            withVoltageAndVoltageDropProfileResult = context.withVoltageMap;
        }
        if (context.withVoltageResult != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVoltageAndVoltageDropProfileResult", version, "1.1");
            withVoltageAndVoltageDropProfileResult = context.withVoltageResult;
        }
        if (context.withFortescueResult != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withFortescueResult", version, "1.1");
            withFortescueResult = context.withFortescueResult;
        }
        if (context.subTransientCoefficient != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: subTransientCoefficient", version, "1.2");
            subTransientCoefficient = context.subTransientCoefficient;
        }
        if (context.withLoads != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withLoads", version, "1.2");
            withLoads = context.withLoads;
        }
        if (context.withShuntCompensators != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withShuntCompensators", version, "1.2");
            withShuntCompensators = context.withShuntCompensators;
        }
        if (context.withVSCConverterStations != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVSCConverterStations", version, "1.2");
            withVSCConverterStations = context.withVSCConverterStations;
        }
        if (context.withNeutralPosition != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withNeutralPosition", version, "1.2");
            withNeutralPosition = context.withNeutralPosition;
        }
        if (context.initialVoltageProfileMode != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: initialVoltageProfileMode", version, "1.2");
            initialVoltageProfileMode = context.initialVoltageProfileMode;
        }
        if (context.voltageRanges != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: voltageRanges", version, "1.2");
            voltageRanges = JsonUtil.readFromNode(context.voltageRanges, deserializationContext, VoltageRange.class, parser.getCodec());
        }

        FaultParameters faultParameters = new FaultParameters(id, withLimitViolations, withVoltageAndVoltageDropProfileResult, withFeederResult, type,
                minVoltageDropProportionalThreshold, withFortescueResult, subTransientCoefficient, withLoads,
                withShuntCompensators, withVSCConverterStations, withNeutralPosition, initialVoltageProfileMode, voltageRanges);
        faultParameters.validate();
        return faultParameters;
    }
}
