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
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.DefaultContingencyList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class DefaultContingencyListDeserializer extends StdDeserializer<DefaultContingencyList> implements ContextualDeserializer {

    private final transient JsonDeserializer<Object> contingenciesDeserializer;

    public DefaultContingencyListDeserializer() {
        this(null);
    }

    public DefaultContingencyListDeserializer(JsonDeserializer<Object> contingenciesDeserializer) {
        super(DefaultContingencyList.class);
        this.contingenciesDeserializer = contingenciesDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) throws JsonMappingException {
        return new DefaultContingencyListDeserializer(JsonUtil.buildListDeserializer(ctxt, property, Contingency.class));
    }

    public DefaultContingencyList deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String name = null;
        List<Contingency> contingencies = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> ctx.setAttribute("version", parser.nextTextValue());
                case "name" -> name = parser.nextTextValue();
                case "type" -> {
                    if (!parser.nextTextValue().equals(DefaultContingencyList.TYPE)) {
                        throw new IllegalStateException("type should be: " + DefaultContingencyList.TYPE);
                    }
                }
                case "contingencies" -> {
                    parser.nextToken();
                    contingencies = JsonUtil.readList(contingenciesDeserializer, ctx, parser, Contingency.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        return new DefaultContingencyList(name, contingencies);
    }
}
