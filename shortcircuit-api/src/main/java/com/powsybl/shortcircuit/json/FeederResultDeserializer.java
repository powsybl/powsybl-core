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
import com.powsybl.shortcircuit.FeederResult;
import com.powsybl.shortcircuit.FortescueFeederResult;
import com.powsybl.shortcircuit.FortescueValue;
import com.powsybl.shortcircuit.MagnitudeFeederResult;

import java.io.IOException;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class FeederResultDeserializer extends StdDeserializer<FeederResult> {

    FeederResultDeserializer() {
        super(FeederResult.class);
    }

    @Override
    public FeederResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String connectableId = null;
        FortescueValue current = null;
        Double currentMagnitude = Double.NaN;

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "connectableId":
                    parser.nextToken();
                    connectableId = parser.readValueAs(String.class);
                    break;

                case "current":
                    parser.nextToken();
                    current = JsonUtil.readValue(deserializationContext, parser, FortescueValue.class);
                    break;

                case "currentMagnitude":
                    parser.nextToken();
                    currentMagnitude = parser.readValueAs(Double.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (current == null) {
            return new MagnitudeFeederResult(connectableId, currentMagnitude);
        } else {
            return new FortescueFeederResult(connectableId, current);
        }
    }
}
