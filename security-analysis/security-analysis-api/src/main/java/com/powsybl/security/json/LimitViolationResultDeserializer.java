/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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
import com.powsybl.security.LimitViolation;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LimitViolationResultDeserializer extends StdDeserializer<LimitViolationsResult> {

    public LimitViolationResultDeserializer() {
        super(LimitViolationsResult.class);
    }

    @Override
    public LimitViolationsResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        boolean computationOk = false;
        List<LimitViolation> limitViolations = Collections.emptyList();
        List<String> actionsTaken = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "computationOk":
                    parser.nextToken();
                    computationOk = parser.readValueAs(Boolean.class);
                    break;

                case "limitViolations":
                    parser.nextToken();
                    limitViolations = JsonUtil.readList(deserializationContext, parser, LimitViolation.class);
                    break;

                case "actionsTaken":
                    parser.nextToken();
                    actionsTaken = JsonUtil.readList(deserializationContext, parser, String.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new LimitViolationsResult(computationOk, limitViolations, actionsTaken);
    }
}
