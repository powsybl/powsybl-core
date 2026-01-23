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
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.powsybl.contingency.list.ContingencyList;
import com.powsybl.contingency.list.DefaultContingencyList;
import com.powsybl.contingency.list.HvdcLineCriterionContingencyList;
import com.powsybl.contingency.list.IdentifierContingencyList;
import com.powsybl.contingency.list.InjectionCriterionContingencyList;
import com.powsybl.contingency.list.LineCriterionContingencyList;
import com.powsybl.contingency.list.ListOfContingencyLists;
import com.powsybl.contingency.list.ThreeWindingsTransformerCriterionContingencyList;
import com.powsybl.contingency.list.TieLineCriterionContingencyList;
import com.powsybl.contingency.list.TwoWindingsTransformerCriterionContingencyList;

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
        while (parser.currentName() != null) {
            if ("type".equals(parser.currentName())) {
                switch (parser.nextTextValue()) {
                    case "default" -> {
                        return (DefaultContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(DefaultContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "injectionCriterion" -> {
                        return (InjectionCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(InjectionCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "hvdcCriterion" -> {
                        return (HvdcLineCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(HvdcLineCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "lineCriterion" -> {
                        return (LineCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(LineCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "twoWindingsTransformerCriterion" -> {
                        return (TwoWindingsTransformerCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(TwoWindingsTransformerCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "threeWindingsTransformerCriterion" -> {
                        return (ThreeWindingsTransformerCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(ThreeWindingsTransformerCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "tieLineCriterion" -> {
                        return (TieLineCriterionContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(TieLineCriterionContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "list" -> {
                        return (ListOfContingencyLists) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(ListOfContingencyLists.class))
                            .deserialize(parser, deserializationContext);
                    }
                    case "identifier" -> {
                        return (IdentifierContingencyList) deserializationContext.findRootValueDeserializer(
                                deserializationContext.constructType(IdentifierContingencyList.class))
                            .deserialize(parser, deserializationContext);
                    }
                    default -> throw new IllegalStateException("Unexpected field: " + parser.currentName());
                }
            }
            parser.nextToken();
        }
        throw new IllegalArgumentException("contingency List needs a type");
    }
}
