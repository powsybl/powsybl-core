/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.Action;
import com.powsybl.security.action.ActionList;

import java.io.IOException;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class ActionListDeserializer extends StdDeserializer<ActionList> {

    public ActionListDeserializer() {
        super(ActionList.class);
    }

    private static class ParsingContext {
        String version;
        List<Action> actions;
    }

    @Override
    public ActionList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "version":
                    context.version = parser.nextTextValue();
                    return true;
                case "actions":
                    parser.nextToken();
                    context.actions =  parser.readValueAs(new TypeReference<List<Action>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        if (context.version == null || !context.version.equals(ActionList.VERSION)) {
            throw new JsonMappingException(parser, "version is missing or not equal to 1.0");
        }
        return new ActionList(context.actions);
    }
}
