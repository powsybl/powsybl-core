/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.list.ListOfContingencyLists;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyListsDeserializer extends StdDeserializer<ListOfContingencyLists> {

    private final ValueDeserializer<Object> contingenciesDeserializer;

    public ListOfContingencyListsDeserializer() {
        this(null);
    }

    public ListOfContingencyListsDeserializer(ValueDeserializer<Object> contingenciesDeserializer) {
        super(ListOfContingencyLists.class);
        this.contingenciesDeserializer = contingenciesDeserializer;
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext deserializationContext,
                                                BeanProperty property) throws DatabindException {
        return new ListOfContingencyListsDeserializer(JsonUtil.buildListDeserializer(deserializationContext, property, ContingencyList.class));
    }

    @Override
    public ListOfContingencyLists deserialize(JsonParser parser, DeserializationContext deserializationContext) throws JacksonException {
        String name = null;
        List<ContingencyList> contingencyLists = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> deserializationContext.setAttribute("version", parser.nextStringValue());
                case "name" -> name = parser.nextStringValue();
                case "type" -> {
                    if (!parser.nextStringValue().equals(ListOfContingencyLists.TYPE)) {
                        throw new IllegalStateException("type should be: " + ListOfContingencyLists.TYPE);
                    }
                }
                case "contingencyLists" -> {
                    parser.nextToken();
                    contingencyLists = JsonUtil.readList(contingenciesDeserializer, deserializationContext, parser, ContingencyList.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }
        return new ListOfContingencyLists(name, contingencyLists);
    }
}
