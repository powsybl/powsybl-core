/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.BusBreakerViolationLocation;
import com.powsybl.security.NodeBreakerViolationLocation;
import com.powsybl.security.ViolationLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.powsybl.security.json.SecurityAnalysisResultDeserializer.SOURCE_VERSION_ATTRIBUTE;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ViolationLocationDeserializer extends StdDeserializer<ViolationLocation> {

    private static final String BUS_ID = "busId";
    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String BUS_BAR_IDS = "busbarIds";
    public static final String SHORT_CIRCUIT_RESULT_VERSION_ATTRIBUTE = "shortCircuitResultVersion";
    private static final String CONTEXT_NAME = "violation-location";

    public ViolationLocationDeserializer() {
        super(ViolationLocation.class);
    }

    @Override
    public ViolationLocation deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String voltageLevelId = null;
        String busId = null;
        List<String> busbarIds = new ArrayList<>();
        String securityResultVersion = JsonUtil.getSourceVersion(deserializationContext, SOURCE_VERSION_ATTRIBUTE);
        String shortCircuitResultVersion = JsonUtil.getSourceVersion(deserializationContext, SHORT_CIRCUIT_RESULT_VERSION_ATTRIBUTE);
        ViolationLocation.Type type = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "type":
                    parser.nextToken();
                    type = JsonUtil.readValue(deserializationContext, parser, ViolationLocation.Type.class);
                    break;
                case BUS_ID:
                    checkVersions(securityResultVersion, shortCircuitResultVersion, BUS_ID);
                    busId = parser.nextTextValue();
                    break;

                case VOLTAGE_LEVEL_ID:
                    checkVersions(securityResultVersion, shortCircuitResultVersion, VOLTAGE_LEVEL_ID);
                    voltageLevelId = parser.nextTextValue();
                    break;

                case BUS_BAR_IDS:
                    checkVersions(securityResultVersion, shortCircuitResultVersion, BUS_BAR_IDS);
                    parser.nextToken();
                    busbarIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        ViolationLocation violationLocation = null;
        if (type == ViolationLocation.Type.NODE_BREAKER) {
            violationLocation = new NodeBreakerViolationLocation(voltageLevelId, busbarIds);
        } else if (type == ViolationLocation.Type.BUS_BREAKER) {
            violationLocation = new BusBreakerViolationLocation(voltageLevelId, busId);
        } else {
            throw new IllegalStateException("type can not be null for ViolationLocation");
        }
        return violationLocation;
    }

    private void checkVersions(String securityResultVersion, String shortCircuitResultVersion, String fieldName) {
        if (securityResultVersion != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, fieldName,
                securityResultVersion, "1.7");
        }
        if (shortCircuitResultVersion != null) {
            JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, fieldName,
                shortCircuitResultVersion, "1.3");
        }
    }
}
