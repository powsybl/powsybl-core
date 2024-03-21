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
import com.powsybl.contingency.contingency.list.ContingencyList;

import java.io.IOException;

/**
 * @author Etienne Lesot {@literal <etienne.lesot@rte-france.com>}
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
                    case "default" -> {
                        DefaultContingencyListDeserializer defaultContingencyListDeserializer = new DefaultContingencyListDeserializer();
                        return defaultContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "injectionCriterion" -> {
                        InjectionCriterionContingencyListDeserializer injectionCriterionContingencyListDeserializer = new InjectionCriterionContingencyListDeserializer();
                        return injectionCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "hvdcCriterion" -> {
                        HvdcLineCriterionContingencyListDeserializer hvdcLineCriterionContingencyListDeserializer = new HvdcLineCriterionContingencyListDeserializer();
                        return hvdcLineCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "lineCriterion" -> {
                        LineCriterionContingencyListDeserializer lineCriterionContingencyListDeserializer = new LineCriterionContingencyListDeserializer();
                        return lineCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "twoWindingsTransformerCriterion" -> {
                        TwoWindingsTransformerCriterionContingencyListDeserializer twoWindingsTransformerCriterionContingencyListDeserializer = new TwoWindingsTransformerCriterionContingencyListDeserializer();
                        return twoWindingsTransformerCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "threeWindingsTransformerCriterion" -> {
                        ThreeWindingsTransformerCriterionContingencyListDeserializer threeWindingsTransformerCriterionContingencyListDeserializer = new ThreeWindingsTransformerCriterionContingencyListDeserializer();
                        return threeWindingsTransformerCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "tieLineCriterion" -> {
                        TieLineCriterionContingencyListDeserializer tieLineCriterionContingencyListDeserializer = new TieLineCriterionContingencyListDeserializer();
                        return tieLineCriterionContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "list" -> {
                        ListOfContingencyListsDeserializer listOfContingencyListsDeserializer = new ListOfContingencyListsDeserializer();
                        return listOfContingencyListsDeserializer.deserialize(parser, deserializationContext);
                    }
                    case "identifier" -> {
                        IdentifierContingencyListDeserializer identifierContingencyListDeserializer = new IdentifierContingencyListDeserializer();
                        return identifierContingencyListDeserializer.deserialize(parser, deserializationContext);
                    }
                    default -> throw new IllegalStateException("Unexpected field: " + parser.getCurrentName());
                }
            }
            parser.nextToken();
        }
        throw new IllegalArgumentException("contingency List needs a type");
    }
}
