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
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.violations.LimitViolation;
import com.powsybl.security.LimitViolationsResult;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class LimitViolationResultDeserializer extends StdDeserializer<LimitViolationsResult> implements ContextualDeserializer {

    private final transient JsonDeserializer<Object> limitViolationsDeserializer;
    private final transient JsonDeserializer<Object> actionsTakenDeserializer;

    public LimitViolationResultDeserializer() {
        this(null, null);
    }

    public LimitViolationResultDeserializer(JsonDeserializer<Object> limitViolationsDeserializer, JsonDeserializer<Object> actionsTakenDeserializer) {
        super(LimitViolationsResult.class);
        this.limitViolationsDeserializer = limitViolationsDeserializer;
        this.actionsTakenDeserializer = actionsTakenDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new LimitViolationResultDeserializer(
            JsonUtil.buildListDeserializer(ctxt, property, LimitViolation.class),
            JsonUtil.buildListDeserializer(ctxt, property, String.class)
        );
    }

    @Override
    public LimitViolationsResult deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        boolean computationOk = false;
        List<LimitViolation> limitViolations = Collections.emptyList();
        List<String> actionsTaken = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "computationOk":
                    parser.nextToken();
                    computationOk = parser.readValueAs(Boolean.class);
                    break;

                case "limitViolations":
                    parser.nextToken();
                    limitViolations = JsonUtil.readList(limitViolationsDeserializer, deserializationContext, parser, LimitViolation.class);
                    break;

                case "actionsTaken":
                    parser.nextToken();
                    actionsTaken = JsonUtil.readList(actionsTakenDeserializer, deserializationContext, parser, String.class);
                    break;

                default:
                    throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        return new LimitViolationsResult(computationOk, limitViolations, actionsTaken);
    }
}
