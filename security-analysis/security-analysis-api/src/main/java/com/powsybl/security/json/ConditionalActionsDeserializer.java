/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.ConditionalActions;

import java.io.IOException;
import java.util.List;

/**
 * @author Bertrand Rix {@literal <bertrand.rix at artelys.com>}
 */
public class ConditionalActionsDeserializer extends StdDeserializer<ConditionalActions> {

    public ConditionalActionsDeserializer() {
        super(ConditionalActions.class);
    }

    private static class ParsingContext {
        String id;
        Condition condition;
        List<String> actionIds;
    }

    @Override
    public ConditionalActions deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ConditionalActionsDeserializer.ParsingContext context = new ConditionalActionsDeserializer.ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "id":
                    parser.nextToken();
                    context.id = parser.getValueAsString();
                    return true;
                case "condition":
                    parser.nextToken();
                    context.condition = JsonUtil.readValue(deserializationContext, parser, Condition.class);
                    return true;
                case "actionIds":
                    parser.nextToken();
                    context.actionIds = JsonUtil.readList(deserializationContext, parser, String.class);
                    return true;
                default:
                    return false;
            }
        });
        return new ConditionalActions(context.id, context.condition, context.actionIds);
    }
}
