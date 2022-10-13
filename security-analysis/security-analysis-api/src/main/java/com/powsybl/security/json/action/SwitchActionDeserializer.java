/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.security.action.SwitchAction;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class SwitchActionDeserializer extends StdDeserializer<SwitchAction> {

    public SwitchActionDeserializer() {
        super(SwitchAction.class);
    }

    private static class ParsingContext {
        String id;
        String switchId;
        Boolean open;
    }

    @Override
    public SwitchAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            switch (jsonParser.getCurrentName()) {
                case "id":
                    context.id = jsonParser.nextTextValue();
                    break;
                case "switchId":
                    context.switchId = jsonParser.nextTextValue();
                    break;
                case "open":
                    jsonParser.nextToken();
                    context.open = jsonParser.getValueAsBoolean();
                    break;
                default:
                    throw new IllegalArgumentException("");
            }
        }
        if (context.open == null) {
            throw JsonMappingException.from(jsonParser, "for switch action open field can't be null");
        }
        return new SwitchAction(context.id, context.switchId, context.open);
    }
}
