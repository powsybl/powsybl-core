/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import com.powsybl.action.AbstractLoadActionBuilder;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.action.DanglingLineAction;
import com.powsybl.action.LoadAction;

import java.io.IOException;

/**
 * @author Anne Tilloy {@literal <anne.tilloy@rte-france.com>}
 */
public abstract class AbstractLoadActionBuilderDeserializer<T extends AbstractLoadActionBuilder> extends StdDeserializer<T> {

    protected AbstractLoadActionBuilderDeserializer(Class<T> vc) {
        super(vc);
    }

    protected static class ParsingContext {
        String id;
        String elementId;
        Boolean relativeValue;
        Double activePowerValue;
        Double reactivePowerValue;
    }

    protected abstract T createAction(ParsingContext context);

    @Override
    public T deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        ParsingContext context = new ParsingContext();
        JsonUtil.parsePolymorphicObject(parser, name -> {
            switch (name) {
                case "type":
                    String typeValue = parser.nextTextValue();
                    if (!LoadAction.NAME.equals(typeValue) && !DanglingLineAction.NAME.equals(typeValue)) {
                        throw JsonMappingException.from(parser, "Expected types " + LoadAction.NAME + ", " + DanglingLineAction.NAME);
                    }
                    return true;
                case "id":
                    context.id = parser.nextTextValue();
                    return true;
                case "loadId", "danglingLineId":
                    context.elementId = parser.nextTextValue();
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
        return createAction(context);
    }
}
