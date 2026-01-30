/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.DefaultContingencyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class DefaultContingencyListDeserializer extends StdDeserializer<DefaultContingencyList> implements ContextualDeserializer {

    private final JsonDeserializer<Object> contingenciesDeserializer;

    public DefaultContingencyListDeserializer() {
        this(null);
    }

    public DefaultContingencyListDeserializer(JsonDeserializer<?> contingenciesDeserializer) {
        super(DefaultContingencyList.class);
        this.contingenciesDeserializer = (JsonDeserializer<Object>) contingenciesDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        JavaType type = ctxt.getTypeFactory().constructCollectionType(ArrayList.class, Contingency.class);
        return new DefaultContingencyListDeserializer(ctxt.findContextualValueDeserializer(type, property));
    }

    public DefaultContingencyList deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String name = null;
        List<Contingency> contingencies = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> parser.nextToken();
                case "name" -> name = parser.nextTextValue();
                case "type" -> {
                    if (!parser.nextTextValue().equals(DefaultContingencyList.TYPE)) {
                        throw new IllegalStateException("type should be: " + DefaultContingencyList.TYPE);
                    }
                }
                case "contingencies" -> {
                    parser.nextToken();
                    contingencies = contingenciesDeserializer != null ?
                        (List<Contingency>) contingenciesDeserializer.deserialize(parser, ctx) :
                        JsonUtil.readList(ctx, parser, Contingency.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        return new DefaultContingencyList(name, contingencies);
    }
}
