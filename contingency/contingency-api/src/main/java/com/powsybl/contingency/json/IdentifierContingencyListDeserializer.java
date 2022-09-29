/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
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
import com.powsybl.contingency.IdentifierContingencyList;
import com.powsybl.contingency.contingency.list.identifiant.Identifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierContingencyListDeserializer extends StdDeserializer<IdentifierContingencyList> {

    public IdentifierContingencyListDeserializer() {
        super(IdentifierContingencyList.class);
    }

    @Override
    public IdentifierContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String identifiableType = null;
        List<Identifier> identifiers = Collections.emptyList();

        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "version":
                    parser.nextToken();
                    break;

                case "name":
                    name = parser.nextTextValue();
                    break;

                case "type":
                    parser.nextToken();
                    break;

                case "identifiableType":
                    identifiableType = parser.nextTextValue();
                    break;

                case "identifiers":
                    parser.nextToken();
                    identifiers = parser.readValueAs(new TypeReference<ArrayList<Identifier>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new IdentifierContingencyList(name, identifiableType, identifiers);
    }
}
