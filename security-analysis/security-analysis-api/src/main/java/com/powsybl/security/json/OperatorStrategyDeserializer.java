/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.OperatorStrategy;

import java.io.IOException;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class OperatorStrategyDeserializer extends StdDeserializer<OperatorStrategy> {

    public OperatorStrategyDeserializer() {
        super(OperatorStrategy.class);
    }

    private static class ParsingContext {
        String id;
        String contingencyId;
        Condition condition;
        List<String> actionIds;
    }

    @Override
    public OperatorStrategy deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "id":
                    parser.nextToken();
                    context.id = parser.getValueAsString();
                    return true;
                case "contingencyId":
                    parser.nextToken();
                    context.contingencyId = parser.getValueAsString();
                    return true;
                case "condition":
                    parser.nextToken();
                    context.condition = parser.readValueAs(Condition.class);
                    return true;
                case "actionIds":
                    parser.nextToken();
                    context.actionIds = parser.readValueAs(new TypeReference<List<String>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        return new OperatorStrategy(context.id, context.contingencyId, context.condition, context.actionIds);
    }
}
