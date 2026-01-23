/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.list.ListOfContingencyLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyListsDeserializer extends StdDeserializer<ListOfContingencyLists> implements ContextualDeserializer {

    private JsonDeserializer<Object> contingenciesDeserializer;

    public ListOfContingencyListsDeserializer() {
        super(ListOfContingencyLists.class);
    }

    public ListOfContingencyListsDeserializer(JsonDeserializer<?> contingenciesDeserializer) {
        super(Contingency.class);
        this.contingenciesDeserializer = (JsonDeserializer<Object>) contingenciesDeserializer;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
                                                BeanProperty property) throws JsonMappingException {
        // Resolve the type for List<ContingencyElement>
        JavaType elementsType = deserializationContext.getTypeFactory()
            .constructCollectionType(ArrayList.class, ContingencyList.class);
        JsonDeserializer<?> deserializer = deserializationContext.findContextualValueDeserializer(elementsType, property);
        return new ListOfContingencyListsDeserializer(deserializer);
    }

    @Override
    public ListOfContingencyLists deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        List<ContingencyList> contingencyLists = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> deserializationContext.setAttribute("version", parser.nextTextValue());
                case "name" -> name = parser.nextTextValue();
                case "type" -> {
                    if (!parser.nextTextValue().equals(ListOfContingencyLists.TYPE)) {
                        throw new IllegalStateException("type should be: " + ListOfContingencyLists.TYPE);
                    }
                }
                case "contingencyLists" -> {
                    parser.nextToken();
                    contingencyLists = contingenciesDeserializer != null ?
                        (List<ContingencyList>) contingenciesDeserializer.deserialize(parser, deserializationContext) :
                        JsonUtil.readList(deserializationContext, parser, ContingencyList.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        return new ListOfContingencyLists(name, contingencyLists);
    }
}
