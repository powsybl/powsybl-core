/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.list.IdentifierContingencyListDeserializer;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
 */
public class ContingencyListDeserializer extends StdDeserializer<ContingencyList> {

    public static final String VERSION = "version";

    public ContingencyListDeserializer() {
        super(ContingencyList.class);
    }

    @Override
    public ContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        JsonNode rootNode = parser.readValueAsTree();

        JsonNode typeNode = rootNode.get("type");
        if (typeNode == null) {
            throw new IllegalArgumentException("contingency list needs a type");
        }
        String type = typeNode.asText();

        JsonParser subParser = rootNode.traverse(parser.getCodec());
        subParser.nextToken(); // START_OBJECT

        return switch (type) {
            case "default" -> new DefaultContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "injectionCriterion" -> new InjectionCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "hvdcCriterion" -> new HvdcLineCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "lineCriterion" -> new LineCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "twoWindingsTransformerCriterion" -> new TwoWindingsTransformerCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "threeWindingsTransformerCriterion" -> new ThreeWindingsTransformerCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "tieLineCriterion" -> new TieLineCriterionContingencyListDeserializer().deserialize(subParser, deserializationContext);
            case "list" -> new ListOfContingencyListsDeserializer().deserialize(subParser, deserializationContext);
            case "identifier" -> new IdentifierContingencyListDeserializer().deserialize(subParser, deserializationContext);
            default -> throw new IllegalStateException("Unexpected type: " + type);
        };
    }
}
