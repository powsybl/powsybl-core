/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.contingency.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import eu.itesla_project.contingency.Contingency;
import eu.itesla_project.contingency.ContingencyElement;
import eu.itesla_project.contingency.ContingencyImpl;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.it>
 */
public class ContingencyDeserializer extends StdDeserializer<Contingency> {

    public ContingencyDeserializer() {
        super(Contingency.class);
    }

    @Override
    public Contingency deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        String id = null;
        List<ContingencyElement> elements = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "id":
                    id = parser.nextTextValue();
                    break;

                case "elements":
                    parser.nextToken();
                    elements = parser.readValueAs(new TypeReference<ArrayList<ContingencyElement>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }

        return new ContingencyImpl(id, elements);
    }
}
