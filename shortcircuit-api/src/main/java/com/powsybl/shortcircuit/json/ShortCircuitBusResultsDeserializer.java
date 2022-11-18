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
import com.powsybl.shortcircuit.*;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class ShortCircuitBusResultsDeserializer extends StdDeserializer<ShortCircuitBusResults> {

    private static final String CONTEXT_NAME = "ShortCircuitBusResults";

    ShortCircuitBusResultsDeserializer() {
        super(ShortCircuitBusResults.class);
    }

    @Override
    public ShortCircuitBusResults deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String version = null;
        String voltageLevelId = null;
        String busId = null;
        FortescueValue voltage = null;
        Double voltageDrop = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version" :
                    parser.nextToken();
                    version = parser.readValueAs(String.class);
                    break;

                case "voltageLevelId":
                    parser.nextToken();
                    voltageLevelId = parser.readValueAs(String.class);
                    break;

                case "busId":
                    parser.nextToken();
                    busId = parser.readValueAs(String.class);
                    break;

                case "voltage":
                    parser.nextToken();
                    voltage = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                    break;

                case "voltageDrop":
                    JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: voltageDrop", version, "1.1");
                    parser.nextToken();
                    voltageDrop = parser.readValueAs(Double.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new ShortCircuitBusResults(voltageLevelId, busId, voltage, voltageDrop);
    }
}
