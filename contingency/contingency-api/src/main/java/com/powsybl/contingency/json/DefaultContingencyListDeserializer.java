/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.json;

import com.powsybl.commons.json.JsonUtil;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.list.DefaultContingencyList;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public class DefaultContingencyListDeserializer extends StdDeserializer<DefaultContingencyList> {

    public DefaultContingencyListDeserializer() {
        super(DefaultContingencyList.class);
    }

    public DefaultContingencyList deserialize(JsonParser parser, DeserializationContext ctx) throws JacksonException {
        String name = null;
        List<Contingency> contingencies = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.currentName()) {
                case "version" -> parser.nextToken();
                case "name" -> name = parser.nextStringValue();
                case "type" -> {
                    if (!parser.nextStringValue().equals(DefaultContingencyList.TYPE)) {
                        throw new IllegalStateException("type should be: " + DefaultContingencyList.TYPE);
                    }
                }
                case "contingencies" -> {
                    parser.nextToken();
                    contingencies = JsonUtil.readList(ctx, parser, Contingency.class);
                }
                default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
            }
        }

        return new DefaultContingencyList(name, contingencies);
    }
}
