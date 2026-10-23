/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.action.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.action.ActionBuilder;
import com.powsybl.action.MultipleActionsAction;
import com.powsybl.action.MultipleActionsActionBuilder;
import com.powsybl.commons.json.JsonUtil;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class MultipleActionsActionBuilderDeserializer extends StdDeserializer<MultipleActionsActionBuilder>
    implements ContextualDeserializer {

    private final transient JsonDeserializer<Object> actionsBuilderDeserializer;

    public MultipleActionsActionBuilderDeserializer() {
        this(null);
    }

    public MultipleActionsActionBuilderDeserializer(JsonDeserializer<Object> actionsBuilderDeserializer) {
        super(MultipleActionsAction.class);
        this.actionsBuilderDeserializer = actionsBuilderDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new MultipleActionsActionBuilderDeserializer(JsonUtil.buildListDeserializer(ctxt, property, ActionBuilder.class));
    }

    @Override
    public MultipleActionsActionBuilder deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        MultipleActionsActionBuilder builder = new MultipleActionsActionBuilder();
        JsonUtil.parsePolymorphicObject(jsonParser, name -> {
            switch (name) {
                case "type":
                    if (!MultipleActionsAction.NAME.equals(jsonParser.nextTextValue())) {
                        throw JsonMappingException.from(jsonParser, "Expected type " + MultipleActionsAction.NAME);
                    }
                    return true;
                case "id":
                    builder.withId(jsonParser.nextTextValue());
                    return true;
                case "actions":
                    jsonParser.nextToken();
                    builder.withActionBuilders(JsonUtil.readList(actionsBuilderDeserializer, deserializationContext, jsonParser, ActionBuilder.class));
                    return true;
                default:
                    return false;
            }
        });
        return builder;
    }
}
