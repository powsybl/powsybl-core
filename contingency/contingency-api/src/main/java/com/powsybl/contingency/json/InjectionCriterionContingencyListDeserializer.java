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
import com.powsybl.contingency.contingency.list.InjectionCriterionContingencyList;
import com.powsybl.contingency.contingency.list.criterion.Criterion;
import com.powsybl.contingency.contingency.list.criterion.PropertyCriterion;
import com.powsybl.contingency.contingency.list.criterion.SingleCountryCriterion;
import com.powsybl.contingency.contingency.list.criterion.SingleNominalVoltageCriterion;

import java.io.IOException;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class InjectionCriterionContingencyListDeserializer extends StdDeserializer<InjectionCriterionContingencyList> {

    public InjectionCriterionContingencyListDeserializer() {
        super(InjectionCriterionContingencyList.class);
    }

    @Override
    public InjectionCriterionContingencyList deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        String name = null;
        String identifiableType = null;
        SingleCountryCriterion countryCriterion = null;
        SingleNominalVoltageCriterion nominalVoltageCriterion = null;
        PropertyCriterion propertyCriterion = null;

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

                case "countryCriterion":
                    parser.nextToken();
                    countryCriterion = parser.readValueAs(new TypeReference<Criterion>() {
                    });
                    break;
                case "nominalVoltageCriterion":
                    parser.nextToken();
                    nominalVoltageCriterion = parser.readValueAs(new TypeReference<Criterion>() {
                    });
                    break;
                case "propertyCriterion":
                    parser.nextToken();
                    propertyCriterion = parser.readValueAs(new TypeReference<Criterion>() {
                    });
                    break;

                default:
                    throw new AssertionError("Unexpected field: " + parser.getCurrentName());
            }
        }
        return new InjectionCriterionContingencyList(name, identifiableType, countryCriterion,
                nominalVoltageCriterion, propertyCriterion);
    }
}
