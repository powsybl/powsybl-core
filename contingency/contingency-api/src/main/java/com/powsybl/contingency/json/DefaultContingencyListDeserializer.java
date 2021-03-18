/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.DefaultContingencyList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public class DefaultContingencyListDeserializer extends StdDeserializer<DefaultContingencyList> {

    public DefaultContingencyListDeserializer() {
        super(DefaultContingencyList.class);
    }

    public DefaultContingencyList deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String name = null;
        List<Contingency> contingencies = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    break;

                case "name":
                    name = parser.nextTextValue();
                    break;

                case "contingencies":
                    parser.nextToken();
                    contingencies = parser.readValueAs(new TypeReference<ArrayList<Contingency>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new DefaultContingencyList(name, contingencies);
    }
}
