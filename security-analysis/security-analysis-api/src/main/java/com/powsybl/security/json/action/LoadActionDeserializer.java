/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json.action;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.security.action.LoadAction;
import com.powsybl.security.action.LoadActionBuilder;

import java.io.IOException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public class LoadActionDeserializer extends StdDeserializer<LoadAction> {

    public LoadActionDeserializer() {
        super(LoadAction.class);
    }

    private static class ParsingContext {
        String id;
        String loadId;
        Boolean relativeValue;
        Double activePowerValue;
        Double reactivePowerValue;
    }

    @Override
    public LoadAction deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    if (!LoadAction.NAME.equals(parser.nextTextValue())) {
                        throw JsonMappingException.from(parser, "Expected type " + LoadAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "loadId":
                    context.loadId = parser.nextTextValue();
                    return true;
                case "relativeValue":
                    parser.nextToken();
                    context.relativeValue = parser.getValueAsBoolean();
                    return true;
                case "activePowerValue":
                    parser.nextToken();
                    context.activePowerValue = parser.getValueAsDouble();
                    return true;
                case "reactivePowerValue":
                    parser.nextToken();
                    context.reactivePowerValue = parser.getValueAsDouble();
                    return true;
                default:
                    return false;
            }
        });
        LoadActionBuilder loadActionBuilder = new LoadActionBuilder();
        loadActionBuilder
                .withId(context.id)
                .withLoadId(context.loadId)
                .withRelativeValue(context.relativeValue);
        if (context.activePowerValue != null) {
            loadActionBuilder.withActivePowerValue(context.activePowerValue);
        }
        if (context.reactivePowerValue != null) {
            loadActionBuilder.withReactivePowerValue(context.reactivePowerValue);
        }
        return loadActionBuilder.build();
    }
}
