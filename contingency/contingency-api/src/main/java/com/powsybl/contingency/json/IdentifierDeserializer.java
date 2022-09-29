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
import com.powsybl.contingency.contingency.list.identifiant.Identifier;
import com.powsybl.contingency.contingency.list.identifiant.IdentifierList;
import com.powsybl.contingency.contingency.list.identifiant.SimpleIdentifier;
import com.powsybl.contingency.contingency.list.identifiant.UcteIdentifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class IdentifierDeserializer extends StdDeserializer<Identifier> {

    public IdentifierDeserializer() {
        super(Identifier.class);
    }

    @Override
    public Identifier deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        Identifier.IdentifierType type = null;
        String identifier = null;
        String voltageLevelId1 = null;
        String voltageLevelId2 = null;
        List<Identifier> identifierList = Collections.emptyList();
        int order = -1;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            switch (parser.getCurrentName()) {
                case "type":
                    type = Identifier.IdentifierType.valueOf(parser.nextTextValue());
                    break;
                case "identifier":
                    identifier = parser.nextTextValue();
                    break;
                case "identifierList":
                    parser.nextToken();
                    identifierList = parser.readValueAs(new TypeReference<ArrayList<Identifier>>() {
                    });
                    break;
                case "voltageLevelId1":
                    voltageLevelId1 = parser.nextTextValue();
                    break;
                case "voltageLevelId2":
                    voltageLevelId2 = parser.nextTextValue();
                    break;
                case "order":
                    order = parser.nextIntValue(-1);
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("type of identifier can not be null");
        }
        switch (type) {
            case SIMPLE:
                return new SimpleIdentifier(identifier);
            case LIST:
                return new IdentifierList(identifierList);
            case UCTE:
                return new UcteIdentifier(voltageLevelId1, voltageLevelId2, order);
            default:
                throw new IllegalArgumentException("type " + type + " does not exist");
        }
    }
}
