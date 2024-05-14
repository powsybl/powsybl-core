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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.contingency.list.ContingencyList;
import com.powsybl.contingency.contingency.list.ListOfContingencyLists;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ListOfContingencyListsDeserializer extends StdDeserializer<ListOfContingencyLists> {

    public ListOfContingencyListsDeserializer() {
        super(ListOfContingencyLists.class);
    }

    @Override
    public ListOfContingencyLists deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        List<ContingencyList> contingencyLists = Collections.emptyList();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version" -> deserializationContext.setAttribute("version", parser.nextTextValue());
                case "name" -> name = parser.nextTextValue();
                case "type" -> {
                    if (!parser.nextTextValue().equals(ListOfContingencyLists.TYPE)) {
                        throw new IllegalStateException("type should be: " + ListOfContingencyLists.TYPE);
                    }
                }
                case "contingencyLists" -> {
                    parser.nextToken();
                    contingencyLists = JsonUtil.readList(deserializationContext, parser, ContingencyList.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new ListOfContingencyLists(name, contingencyLists);
    }
}
