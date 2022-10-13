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
import com.powsybl.security.action.LineConnectionAction;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class LineConnectionActionDeserializer extends StdDeserializer<LineConnectionAction> {

    public LineConnectionActionDeserializer() {
        super(LineConnectionAction.class);
    }

    private static class ParsingContext {
        String id;
        String lineId;
        Boolean openSide1;
        Boolean openSide2;
    }

    @Override
    public LineConnectionAction deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            switch (jsonParser.getCurrentName()) {
                case "id":
                    context.id = jsonParser.nextTextValue();
                    break;
                case "lineId":
                    context.lineId = jsonParser.nextTextValue();
                    break;
                case "openSide1":
                    jsonParser.nextToken();
                    context.openSide1 = jsonParser.getValueAsBoolean();
                    break;
                case "openSide2":
                    jsonParser.nextToken();
                    context.openSide2 = jsonParser.getValueAsBoolean();
                    break;
                default:
                    throw new IllegalArgumentException("");
            }
        }
        if (context.openSide1 == null || context.openSide2 == null) {
            throw JsonMappingException.from(jsonParser, "for line action openSide1 and openSide2 fields can't be null");
        }
        return new LineConnectionAction(context.id, context.lineId, context.openSide1, context.openSide2);
    }
}
