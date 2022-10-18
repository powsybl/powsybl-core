/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.Action;
import com.powsybl.security.action.MultipleActionsAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class MultipleActionsActionDeserializer extends StdDeserializer<MultipleActionsAction> {

    public MultipleActionsActionDeserializer() {
        super(MultipleActionsAction.class);
    }

    private static class ParsingContext {
        String id;
        List<Action> actions;
    }

    @Override
    public MultipleActionsAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!MultipleActionsAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + MultipleActionsAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "actions":
                    jsonParser.nextToken();
                    context.actions = jsonParser.readValueAs(new TypeReference<ArrayList<Action>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        return new MultipleActionsAction(context.id, context.actions);
    }
}
