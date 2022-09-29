/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.ContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class ContingencyListDeserializer extends StdDeserializer<ContingencyList> {

    public ContingencyListDeserializer() {
        super(ContingencyList.class);
    }

    @Override
    public ContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        parser.nextToken();
        while (parser.getCurrentName() != null) {
            if ("type".equals(parser.getCurrentName())) {
                switch (parser.nextTextValue()) {
                    case "default":
                        DefaultContingencyListDeserializer defaultContingencyListDeserializer = new DefaultContingencyListDeserializer();
                        return defaultContingencyListDeserializer.deserialize(parser, deserializationContext);
                    case "criterion":
                        CriterionContingencyListDeserializer criterionContingencyListDeserializer = new CriterionContingencyListDeserializer();
                        return criterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    case "list":
                        ContingencyListsListDeserializer contingencyListsListDeserializer = new ContingencyListsListDeserializer();
                        return contingencyListsListDeserializer.deserialize(parser, deserializationContext);
                    default:
                        throw new AssertionError("Unexpected field: " + parser.getCurrentName());
                }
            }
            parser.nextToken();
        }
        throw new IllegalArgumentException("contingency List needs a type");
    }
}
