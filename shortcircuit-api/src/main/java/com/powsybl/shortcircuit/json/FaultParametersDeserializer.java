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
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.ShortCircuitConstants;
import com.powsybl.shortcircuit.StudyType;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class FaultParametersDeserializer extends StdDeserializer<FaultParameters> {

    FaultParametersDeserializer() {
        super(FaultParameters.class);
    }

    @Override
    public FaultParameters deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String id = null;
        boolean withLimitViolations = false;
        boolean withVoltageMap = false;
        boolean withFeederResult = false;
        StudyType type = null;
        double subTransStudyReactanceCoefficient = Double.NaN;
        double minVoltageDropProportionalThreshold = Double.NaN;
        ShortCircuitConstants.VoltageMapType voltageMapType = null;
        boolean useResistances = false;
        boolean useLoads = false;
        boolean useCapacities = false;
        boolean useShunts = false;
        boolean useTapChangers = false;
        boolean useMutuals = false;
        boolean modelVSC = false;
        ShortCircuitConstants.StartedGroups startedGroupsInsideZone = null;
        double startedGroupsInsideZoneThreshold = Double.NaN;
        ShortCircuitConstants.StartedGroups startedGroupsOutOfZone = null;
        double startedGroupsOutOfZoneThreshold = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
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

                case "subTransStudyReactanceCoefficient":
                    parser.nextToken();
                    subTransStudyReactanceCoefficient = parser.readValueAs(Double.class);
                    break;

                case "minVoltageDropProportionalThreshold":
                    parser.nextToken();
                    minVoltageDropProportionalThreshold = parser.readValueAs(Double.class);
                    break;

                case "voltageMapType":
                    parser.nextToken();
                    voltageMapType = ShortCircuitConstants.VoltageMapType.valueOf(parser.readValueAs(String.class));

                case "useResistances":
                    parser.nextToken();
                    useResistances = parser.readValueAs(Boolean.class);
                    break;

                case "useLoads":
                    parser.nextToken();
                    useLoads = parser.readValueAs(Boolean.class);
                    break;

                case "useCapacities":
                    parser.nextToken();
                    useCapacities = parser.readValueAs(Boolean.class);
                    break;

                case "useShunts":
                    parser.nextToken();
                    useShunts = parser.readValueAs(Boolean.class);
                    break;

                case "useTapChangers":
                    parser.nextToken();
                    useTapChangers = parser.readValueAs(Boolean.class);
                    break;

                case "useMutuals":
                    parser.nextToken();
                    useMutuals = parser.readValueAs(Boolean.class);
                    break;

                case "modelVSC":
                    parser.nextToken();
                    modelVSC = parser.readValueAs(Boolean.class);
                    break;

                case "startedGroupsInsideZone":
                    parser.nextToken();
                    startedGroupsInsideZone = ShortCircuitConstants.StartedGroups.valueOf(parser.readValueAs(String.class));
                    break;

                case "startedGroupsInsideZoneThreshold":
                    parser.nextToken();
                    startedGroupsInsideZoneThreshold = parser.readValueAs(Double.class);
                    break;

                case "startedGroupsOutOfZone":
                    parser.nextToken();
                    startedGroupsOutOfZone = ShortCircuitConstants.StartedGroups.valueOf(parser.readValueAs(String.class));
                    break;

                case "startedGroupsOutOfZoneThreshold":
                    parser.nextToken();
                    startedGroupsOutOfZoneThreshold = parser.readValueAs(Double.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new FaultParameters(id, withLimitViolations, withVoltageMap, withFeederResult, type, subTransStudyReactanceCoefficient, minVoltageDropProportionalThreshold, voltageMapType, useResistances, useLoads, useCapacities, useShunts, useTapChangers, useMutuals, modelVSC, startedGroupsInsideZone, startedGroupsInsideZoneThreshold, startedGroupsOutOfZone, startedGroupsOutOfZoneThreshold);
    }
}
