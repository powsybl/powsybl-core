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
import com.powsybl.shortcircuit.ConfiguredInitialVoltageProfileCoefficient;
import com.powsybl.shortcircuit.FaultParameters;
import com.powsybl.shortcircuit.InitialVoltageProfile;
import com.powsybl.shortcircuit.StudyType;

import java.io.IOException;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
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
        InitialVoltageProfile initialVoltageProfile = null;
        List<ConfiguredInitialVoltageProfileCoefficient> coefficients = null;

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
                    JsonUtil.assertLessThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMap", version, "1.1");
                    parser.nextToken();
                    withVoltageAndVoltageDropProfileResult = parser.readValueAs(Boolean.class);
                    break;
                case "withVoltageResult":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: withVoltageAndVoltageDropProfileResult", version, "1.1");
                    parser.nextToken();
                    withVoltageAndVoltageDropProfileResult = parser.readValueAs(Boolean.class);
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
                case "withFortescueResult":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.1");
                    parser.nextToken();
                    withFortescueResult = parser.readValueAs(Boolean.class);
                    break;
                case "subTransientCoefficient":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    subTransientCoefficient = parser.readValueAs(Double.class);
                    break;
                case "withLoads":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withLoads = parser.readValueAs(Boolean.class);
                    break;
                case "withShuntCompensators":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withShuntCompensators = parser.readValueAs(Boolean.class);
                    break;
                case "withVSCConverterStations":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withVSCConverterStations = parser.readValueAs(Boolean.class);
                    break;
                case "withNeutralPosition":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    withNeutralPosition = parser.readValueAs(Boolean.class);
                    break;
                case "initialVoltageProfile":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    parser.nextToken();
                    initialVoltageProfile = InitialVoltageProfile.valueOf(parser.readValueAs(String.class));
                    break;
                case "configuredInitialVoltageRangeCoefficients":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, TAG + parser.getCurrentName(), version, "1.2");
                    coefficients = new ConfiguredInitialVoltageProfileCoefficientDeserializer().deserialize(parser);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new FaultParameters(id, withLimitViolations, withVoltageAndVoltageDropProfileResult, withFeederResult, type,
                minVoltageDropProportionalThreshold, withFortescueResult, subTransientCoefficient, withLoads,
                withShuntCompensators, withVSCConverterStations, withNeutralPosition, initialVoltageProfile, coefficients);
    }
}
