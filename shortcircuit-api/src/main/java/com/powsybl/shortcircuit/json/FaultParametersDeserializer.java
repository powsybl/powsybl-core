/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.ShortCircuitConstants;
import com.powsybl.shortcircuit.StudyType;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class FaultParametersDeserializer extends StdDeserializer<FaultParameters> {

    private static final String CONTEXT_NAME = "ShortCircuitFaultParameters";

    FaultParametersDeserializer() {
        super(FaultParameters.class);
    }

    @Override
    public FaultParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = null;
        String id = null;
        boolean withLimitViolations = false;
        boolean withVoltageMap = false;
        boolean withFeederResult = false;
        StudyType type = null;
        double minVoltageDropProportionalThreshold = Double.NaN;
        ShortCircuitConstants.VoltageMapType voltageMapType = null;
        ShortCircuitConstants.NominalVoltageMapType nominalVoltageMapType = null;
        boolean useResistances = false;
        boolean useLoads = false;
        boolean useCapacities = false;
        boolean useShunts = false;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    version = parser.getValueAsString();
                    break;
                case "id":
                    parser.nextToken();
                    id = parser.readValueAs(String.class);
                    break;

                case "withLimitViolations":
                    parser.nextToken();
                    withLimitViolations = parser.readValueAs(Boolean.class);
                    break;

                case "withVoltageMap":
                    parser.nextToken();
                    withVoltageMap = parser.readValueAs(Boolean.class);
                    break;

                case "withFeederResult":
                    parser.nextToken();
                    withFeederResult = parser.readValueAs(Boolean.class);
                    break;

                case "studyType":
                    parser.nextToken();
                    type = StudyType.valueOf(parser.readValueAs(String.class));
                    break;

                case "minVoltageDropProportionalThreshold":
                    parser.nextToken();
                    minVoltageDropProportionalThreshold = parser.readValueAs(Double.class);
                    break;

                case "voltageMapType":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMapType" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    voltageMapType = parser.readValueAs(ShortCircuitConstants.VoltageMapType.class);
                    break;

                case "nominalVoltageMapType":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: nominalVoltageMapType" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    nominalVoltageMapType = parser.readValueAs(ShortCircuitConstants.NominalVoltageMapType.class);
                    break;
                case "useResistances":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useResistances" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    useResistances = parser.readValueAs(Boolean.class);
                    break;

                case "useLoads":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useLoads" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    useLoads = parser.readValueAs(Boolean.class);
                    break;

                case "useCapacities":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useCapacities" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    useCapacities = parser.readValueAs(Boolean.class);
                    break;

                case "useShunts":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: useShunts" + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    useShunts = parser.readValueAs(Boolean.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new FaultParameters(id, withLimitViolations, withVoltageMap, withFeederResult, type, minVoltageDropProportionalThreshold,
                voltageMapType, nominalVoltageMapType, useResistances, useLoads, useCapacities, useShunts);
    }
}
