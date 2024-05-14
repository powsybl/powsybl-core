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
                case "version" -> {
                    parser.nextToken();
                    version = parser.getValueAsString();
                    JsonUtil.setSourceVersion(deserializationContext, version, SOURCE_VERSION_ATTRIBUTE);
                    deserializationContext.setAttribute(SOURCE_PARAMETER_TYPE_ATTRIBUTE, ParametersType.SHORT_CIRCUIT);
                }
                case "withLimitViolations" -> {
                    parser.nextToken();
                    parameters.setWithLimitViolations(parser.readValueAs(Boolean.class));
                }
                case "withVoltageMap" -> {
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    parameters.setWithVoltageResult(parser.readValueAs(Boolean.class));
                }
                case "withVoltageResult" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    parameters.setWithVoltageResult(parser.readValueAs(Boolean.class));
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
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    parameters.setWithFortescueResult(parser.readValueAs(Boolean.class));
                }
                case "subTransientCoefficient" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setSubTransientCoefficient(parser.readValueAs(Double.class));
                }
                case "withLoads" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setWithLoads(parser.readValueAs(Boolean.class));
                }
                case "withShuntCompensators" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setWithShuntCompensators(parser.readValueAs(Boolean.class));
                }
                case "withVSCConverterStations" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setWithVSCConverterStations(parser.readValueAs(Boolean.class));
                }
                case "withNeutralPosition" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setWithNeutralPosition(parser.readValueAs(Boolean.class));
                }
                case "initialVoltageProfileMode" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setInitialVoltageProfileMode(JsonUtil.readValue(deserializationContext, parser, InitialVoltageProfileMode.class));
                }
                case "voltageRanges" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    parameters.setVoltageRanges(JsonUtil.readList(deserializationContext, parser, VoltageRange.class));
                }
                case "detailedReport" -> {
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.3");
                    parser.nextToken();
                    parameters.setDetailedReport(parser.readValueAs(Boolean.class));
                }
                case "extensions" -> {
                    parser.nextToken();
                    extensions = JsonUtil.updateExtensions(parser, deserializationContext, getExtensionSerializers()::get, parameters);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        extensions.forEach(extension -> parameters.addExtension((Class) extension.getClass(), extension));
        parameters.validate();
        return parameters;
    }

}
