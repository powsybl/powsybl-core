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
import com.powsybl.contingency.CriterionContingencyList;
import com.powsybl.contingency.DefaultContingencyList;
import com.powsybl.contingency.contingency.list.criterion.Criterion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class CriterionContingencyListDeserializer extends StdDeserializer<CriterionContingencyList> {

    public CriterionContingencyListDeserializer() {
        super(DefaultContingencyList.class);
    }

    @Override
    public CriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String identifiableType = null;
        List<Criterion> criteria = Collections.emptyList();

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

                case "criteria":
                    parser.nextToken();
                    criteria = parser.readValueAs(new TypeReference<ArrayList<Criterion>>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new CriterionContingencyList(name, identifiableType, criteria);
    }
}
