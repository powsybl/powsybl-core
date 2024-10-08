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

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
public class ViolationLocationDeserializer extends StdDeserializer<ViolationLocation> {

    private static final String BUS_ID = "busId";
    private static final String VOLTAGE_LEVEL_ID = "voltageLevelId";
    private static final String BUS_BAR_IDS = "busbarIds";

    public ViolationLocationDeserializer() {
        super(ViolationLocation.class);
    }

    @Override
    public ViolationLocation deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String voltageLevelId = null;
        String busId = null;
        List<String> busbarIds = new ArrayList<>();
        ViolationLocation.Type type = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "type":
                    parser.nextToken();
                    type = JsonUtil.readValue(deserializationContext, parser, ViolationLocation.Type.class);
                    break;
                case BUS_ID:
                    busId = parser.nextTextValue();
                    break;

                case VOLTAGE_LEVEL_ID:
                    voltageLevelId = parser.nextTextValue();
                    break;

                case BUS_BAR_IDS:
                    parser.nextToken();
                    busbarIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;
                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        if (type == ViolationLocation.Type.NODE_BREAKER) {
            return new NodeBreakerViolationLocation(voltageLevelId, busbarIds);
        } else if (type == ViolationLocation.Type.BUS_BREAKER) {
            return new BusBreakerViolationLocation(voltageLevelId, busId);
        } else {
            throw new IllegalStateException("type should be among [NODE_BREAKER, BUS_BREAKER].");
        }
    }
}
