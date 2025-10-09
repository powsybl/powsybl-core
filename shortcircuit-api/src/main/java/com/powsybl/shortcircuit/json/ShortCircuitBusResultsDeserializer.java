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
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.shortcircuit.FortescueValue;
import com.powsybl.shortcircuit.FortescueShortCircuitBusResults;
import com.powsybl.shortcircuit.ShortCircuitBusResults;
import com.powsybl.shortcircuit.MagnitudeShortCircuitBusResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class ShortCircuitBusResultsDeserializer {

    private static final String CONTEXT_NAME = "ShortCircuitBusResults";

    public List<ShortCircuitBusResults> deserialize(JsonParser parser, String version) throws IOException {
        List<ShortCircuitBusResults> shortCircuitBusResults = new ArrayList<>();
        parser.nextToken();
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            String voltageLevelId = null;
            String busId = null;
            Double initialVoltageMagnitude = Double.NaN;
            FortescueValue voltage = null;
            Double voltageMagnitude = Double.NaN;
            Double voltageDropProportional = Double.NaN;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                switch (parser.currentName()) {
                    case "voltageLevelId" -> {
                        parser.nextToken();
                        voltageLevelId = parser.readValueAs(String.class);
                    }
                    case "busId" -> {
                        parser.nextToken();
                        busId = parser.readValueAs(String.class);
                    }
                    case "initialVoltageMagnitude" -> {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: initialVoltageMagnitude", version, "1.1");
                        parser.nextToken();
                        initialVoltageMagnitude = parser.readValueAs(Double.class);
                    }
                    case "voltage" -> {
                        parser.nextToken();
                        voltage = parser.readValueAs(FortescueValue.class);
                    }
                    case "voltageMagnitude" -> {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: voltageMagnitude", version, "1.1");
                        parser.nextToken();
                        voltageMagnitude = parser.readValueAs(Double.class);
                    }
                    case "voltageDropProportional" -> {
                        JsonUtil.assertGreaterOrEqualThanReferenceVersion(CONTEXT_NAME, "Tag: voltageDropProportional", version, "1.1");
                        parser.nextToken();
                        voltageDropProportional = parser.readValueAs(Double.class);
                    }
                    default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
                }
            }
            if (voltage != null) {
                shortCircuitBusResults.add(new FortescueShortCircuitBusResults(voltageLevelId, busId, initialVoltageMagnitude, voltage, voltageDropProportional));
            } else {
                shortCircuitBusResults.add(new MagnitudeShortCircuitBusResults(voltageLevelId, busId, initialVoltageMagnitude, voltageMagnitude, voltageDropProportional));
            }
        }
        return shortCircuitBusResults;
    }
}
