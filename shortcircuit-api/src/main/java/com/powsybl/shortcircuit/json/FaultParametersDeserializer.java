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
    private static final String TAG = "TAG: ";

    FaultParametersDeserializer() {
        super(FaultParameters.class);
    }

    @Override
    public FaultParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
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
            switch (parser.getCurrentName()) {
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
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMap", version, "1.1");
                    parser.nextToken();
                    withVoltageAndVoltageDropProfileResult = parser.readValueAs(Boolean.class);
                }
                case "withVoltageResult" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVoltageAndVoltageDropProfileResult", version, "1.1");
                    parser.nextToken();
                    withVoltageAndVoltageDropProfileResult = parser.readValueAs(Boolean.class);
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
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    withFortescueResult = parser.readValueAs(Boolean.class);
                }
                case "subTransientCoefficient" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    subTransientCoefficient = parser.readValueAs(Double.class);
                }
                case "withLoads" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withLoads = parser.readValueAs(Boolean.class);
                }
                case "withShuntCompensators" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withShuntCompensators = parser.readValueAs(Boolean.class);
                }
                case "withVSCConverterStations" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withVSCConverterStations = parser.readValueAs(Boolean.class);
                }
                case "withNeutralPosition" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withNeutralPosition = parser.readValueAs(Boolean.class);
                }
                case "initialVoltageProfileMode" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    initialVoltageProfileMode = InitialVoltageProfileMode.valueOf(parser.readValueAs(String.class));
                }
                case "voltageRanges" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    voltageRanges = JsonUtil.readList(deserializationContext, parser, VoltageRange.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        FaultParameters faultParameters = new FaultParameters(id, withLimitViolations, withVoltageAndVoltageDropProfileResult, withFeederResult, type,
                minVoltageDropProportionalThreshold, withFortescueResult, subTransientCoefficient, withLoads,
                withShuntCompensators, withVSCConverterStations, withNeutralPosition, initialVoltageProfileMode, voltageRanges);
        faultParameters.validate();
        return faultParameters;
    }
}
