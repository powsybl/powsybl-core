/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.iidm.network.ThreeSides;
import com.powsybl.action.TerminalsConnectionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class TerminalsConnectionActionDeserializer extends StdDeserializer<TerminalsConnectionAction> {

    public TerminalsConnectionActionDeserializer() {
        super(TerminalsConnectionAction.class);
    }

    private static class ParsingContext {
        String id;
        String elementId;
        ThreeSides side;
        Boolean open;
    }

    @Override
    public TerminalsConnectionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!TerminalsConnectionAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + TerminalsConnectionAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = jsonParser.nextTextValue();
                    return true;
                case "elementId":
                    context.elementId = jsonParser.nextTextValue();
                    return true;
                case "side":
                    jsonParser.nextToken();
                    context.side = ThreeSides.valueOf(jsonParser.getValueAsString());
                    return true;
                case "open":
                    jsonParser.nextToken();
                    context.open = jsonParser.getValueAsBoolean();
                    return true;
                default:
                    return false;
            }
        });
        if (context.open == null) {
            throw JsonMappingException.from(jsonParser, "for terminal connection action open field can't be null");
        }
        if (context.side == null) {
            return new TerminalsConnectionAction(context.id, context.elementId, context.open);
        } else {
            return new TerminalsConnectionAction(context.id, context.elementId, context.side, context.open);
        }
    }
}
