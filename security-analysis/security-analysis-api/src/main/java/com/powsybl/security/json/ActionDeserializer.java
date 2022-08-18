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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.Action;
import com.powsybl.security.action.LineAction;
import com.powsybl.security.action.MultipleActionsAction;
import com.powsybl.security.action.SwitchAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ActionDeserializer extends StdDeserializer<Action> {

    public ActionDeserializer() {
        super(Action.class);
    }

    private static class ParsingContext {
        String type;
        String id;
        String switchId;
        String lineId;
        Boolean open;
        Boolean openSide1;
        Boolean openSide2;
        List<Action> actions;
    }

    @Override
    public Action deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parseObject(parser, fieldName -> {
            switch (fieldName) {
                case "type":
                    context.type = parser.nextTextValue();
                    return true;
                case "id":
                    context.id =  parser.nextTextValue();
                    return true;
                case "switchId":
                    context.switchId =  parser.nextTextValue();
                    return true;
                case "lineId":
                    context.lineId =  parser.nextTextValue();
                    return true;
                case "open":
                    parser.nextToken();
                    context.open =  parser.getBooleanValue();
                    return true;
                case "openSide1":
                    parser.nextToken();
                    context.openSide1 =  parser.getBooleanValue();
                    return true;
                case "openSide2":
                    parser.nextToken();
                    context.openSide2 =  parser.getBooleanValue();
                    return true;
                case "actions":
                    parser.nextToken();
                    context.actions = parser.readValueAs(new TypeReference<ArrayList<Action>>() {
                    });
                    return true;
                default:
                    return false;
            }
        });
        if (context.type == null) {
            throw JsonMappingException.from(parser, "action type can not be null");
        }
        switch (context.type) {
            case SwitchAction.NAME:
                if (context.open == null) {
                    throw JsonMappingException.from(parser, "for switch action open field can't be null");
                }
                return new SwitchAction(context.id, context.switchId, context.open);
            case LineAction.NAME:
                if (context.openSide1 == null && context.openSide2 == null) {
                    throw JsonMappingException.from(parser, "for line action openSide1 and openSide2 fields can't be null");
                }
                return new LineAction(context.id, context.lineId, context.openSide1, context.openSide2);
            case MultipleActionsAction.NAME:
                return new MultipleActionsAction(context.id, context.actions);
            default:
                throw JsonMappingException.from(parser, "Unknown action type: " + context.type);
        }
    }
}
