/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class LimitViolationResultDeserializer extends StdDeserializer<LimitViolationsResult> {

    LimitViolationResultDeserializer() {
        super(LimitViolationsResult.class);
    }

    @Override
    public LimitViolationsResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        boolean comutationOk = false;
        List<LimitViolation> limitViolations = Collections.emptyList();
        List<String> actionsTaken = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "computationOk":
                    parser.nextToken();
                    comutationOk = parser.readValueAs(Boolean.class);
                    break;

                case "limitViolations":
                    parser.nextToken();
                    limitViolations = parser.readValueAs(new TypeReference<ArrayList<LimitViolation>>() {
                    });
                    break;

                case "actionsTaken":
                    parser.nextToken();
                    actionsTaken = parser.readValueAs(new TypeReference<ArrayList<String>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new LimitViolationsResult(comutationOk, limitViolations, actionsTaken);
    }
}
