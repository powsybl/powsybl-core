/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.shortcircuit.FaultParameters;

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

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new FaultParameters(id, withLimitViolations, withVoltageMap);
    }
}
