/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.SwitchAction;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
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
    public SwitchAction deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!SwitchAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + SwitchAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "switchId":
                    context.switchId = parser.nextTextValue();
                    return true;
                case "open":
                    parser.nextToken();
                    context.open = parser.getValueAsBoolean();
                    return true;
                default:
                    return false;
            }
        });
        if (context.open == null) {
            throw JsonMappingException.from(parser, "for switch action open field can't be null");
        }
        return new SwitchAction(context.id, context.switchId, context.open);
    }
}
