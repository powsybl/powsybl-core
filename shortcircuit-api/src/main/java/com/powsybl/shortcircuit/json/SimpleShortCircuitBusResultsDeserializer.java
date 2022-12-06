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
import com.powsybl.shortcircuit.SimpleShortCircuitBusResults;

import java.io.IOException;

/**
 * @author Coline Piloquet <coline.piloquet at rte-france.com>
 */
public class SimpleShortCircuitBusResultsDeserializer extends StdDeserializer<SimpleShortCircuitBusResults> {
    private static final String CONTEXT_NAME = "SimpleShortCircuitBusResults";

    SimpleShortCircuitBusResultsDeserializer() {
        super(SimpleShortCircuitBusResults.class);
    }

    @Override
    public SimpleShortCircuitBusResults deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String voltageLevelId = null;
        String busId = null;
        Double voltage = Double.NaN;
        Double voltageDropProportional = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken(); //skip
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
                    voltage = parser.readValueAs(Double.class);
                    break;

                case "voltageDropProportional":
                    parser.nextToken();
                    voltageDropProportional = parser.readValueAs(Double.class);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new SimpleShortCircuitBusResults(voltageLevelId, busId, voltage, voltageDropProportional);
    }
}
